package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.KeyPair;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the user profile resource. Each process waiting for get / put is added to a queue and delivered in
 * order.
 * 
 * @author Nico, Seppi
 * 
 */
public class UserProfileManager {

	private final static Logger logger = LoggerFactory.getLogger(UserProfileManager.class);
	private static final long MAX_MODIFICATION_TIME = 1000;

	private final NetworkManager networkManager;
	private final UserCredentials credentials;
	private final SecretKey userProfileEncryptionKey;

	private final Object queueWaiter = new Object();
	private final QueueWorker worker = new QueueWorker();
	private final Queue<QueueEntry> readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
	private final Queue<PutQueueEntry> modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();

	private volatile PutQueueEntry modifying;

	private boolean running = true;
	private KeyPair defaultProtectionKey = null;

	public UserProfileManager(NetworkManager networkManager, UserCredentials credentials) {
		this.networkManager = networkManager;
		this.credentials = credentials;
		// needs to be done only once
		this.userProfileEncryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
				credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);

		Thread thread = new Thread(worker);
		thread.setName("UP queue");
		thread.start();
	}

	public void stopQueueWorker() {
		running = false;
		synchronized (queueWaiter) {
			queueWaiter.notify();
		}
	}

	public UserCredentials getUserCredentials() {
		return credentials;
	}

	/**
	 * Gets the user profile. The call blocks until the most recent profile is here.
	 * 
	 * @param pid the process identifier
	 * @param intendsToPut whether the process intends modifying and putting the user profile. After the
	 *            get-call, the profile has a given time to make its modification.
	 * @param the user profile
	 * @throws GetFailedException if the profile cannot be fetched
	 */
	public UserProfile getUserProfile(String pid, boolean intendsToPut) throws GetFailedException {
		QueueEntry entry;

		if (intendsToPut) {
			PutQueueEntry putEntry = new PutQueueEntry(pid);
			modifyQueue.add(putEntry);
			entry = putEntry;
		} else {
			entry = new QueueEntry(pid);
			readOnlyQueue.add(entry);
		}

		synchronized (queueWaiter) {
			queueWaiter.notify();
		}

		try {
			entry.waitForGet();
		} catch (GetFailedException e) {
			// just stop the modification if an error occurs.
			if (intendsToPut)
				stopModification(pid);
			throw e;
		}

		UserProfile profile = entry.getUserProfile();
		if (profile == null)
			throw new GetFailedException("User Profile not found");
		return profile;
	}

	/**
	 * A process notifies that he is ready to put the new profile. Note that the profile in the argument must
	 * be a modification of the profile in the DHT.
	 * 
	 * @param profile the modified user profile
	 * @param pid the process identifier
	 * @throws PutFailedException if putting has failed (because of network errors or the profile is invalid).
	 *             An error is also thrown when the process is not allowed to put (because he did not register
	 *             himself as intending to put)
	 */
	public void readyToPut(UserProfile profile, String pid) throws PutFailedException {
		if (modifying != null && modifying.equals(pid)) {
			modifying.setUserProfile(profile);
			modifying.readyToPut();
			modifying.waitForPut();
		} else {
			throw new PutFailedException("Not allowed to put anymore");
		}
	}

	/**
	 * Notifies that a process is done with a modification on the user profile.
	 */
	private void stopModification(String pid) {
		// test whether is the current modifying process
		if (modifying != null && modifying.equals(pid)) {
			modifying.abort();
		}
	}

	/**
	 * Get the default content protection keys. If called first time the method is called first time the user
	 * profile gets loaded from network and the default protection key temporally gets stored for further
	 * gets.
	 * 
	 * @return the default content protection keys
	 * @throws GetFailedException
	 */
	public KeyPair getDefaultProtectionKey() throws GetFailedException {
		if (defaultProtectionKey == null) {
			UserProfile userProfile = getUserProfile(UUID.randomUUID().toString(), false);
			defaultProtectionKey = userProfile.getProtectionKeys();
		}
		return defaultProtectionKey;
	}

	private class QueueWorker implements Runnable {

		private UserProfile cachedUserProfile = null;

		@Override
		public void run() {
			while (running) { // run forever
				// modifying processes have advantage here because the read-only processes can profit
				if (modifyQueue.isEmpty() && readOnlyQueue.isEmpty()) {
					synchronized (queueWaiter) {
						try {
							queueWaiter.wait();
						} catch (InterruptedException e) {
							// ignore
						}
					}
				} else if (modifyQueue.isEmpty()) {
					logger.trace("{} process(es) are waiting for read-only access.", readOnlyQueue.size());
					// a process wants to read
					QueueEntry entry = readOnlyQueue.peek();

					get(entry);

					logger.trace("Notifying {} processes that newest profile is ready.", readOnlyQueue.size());
					// notify all read only processes
					while (!readOnlyQueue.isEmpty()) {
						// copy user profile and errors to other entries
						QueueEntry readOnly = readOnlyQueue.poll();
						readOnly.setUserProfile(entry.getUserProfile());
						readOnly.setGetError(entry.getGetError());
						readOnly.notifyGet();
					}
				} else {
					// a process wants to modify
					modifying = modifyQueue.poll();
					logger.trace("Process {} is waiting to make profile modifications.", modifying.getPid());
					get(modifying);
					logger.trace("Notifying {} processes (inclusive process {}) to get newest profile.",
							readOnlyQueue.size() + 1, modifying.getPid());

					modifying.notifyGet();
					// notify all read only processes
					while (!readOnlyQueue.isEmpty()) {
						// copy user profile and errors to other entries
						QueueEntry readOnly = readOnlyQueue.poll();
						readOnly.setUserProfile(modifying.getUserProfile());
						readOnly.setGetError(modifying.getGetError());
						readOnly.notifyGet();
					}

					int counter = 0;
					long sleepTime = MAX_MODIFICATION_TIME / 10;
					while (counter < 10 && !modifying.isReadyToPut() && !modifying.isAborted()) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							// ignore
						} finally {
							counter++;
						}
					}

					if (modifying.isReadyToPut()) {
						// is ready to put
						logger.trace("Process {} made modifcations and uploads them now.", modifying.getPid());
						put(modifying);
					} else if (!modifying.isAborted()) {
						// request is not ready to put and has not been aborted
						logger.warn("Process {} never finished doing modifications. Abort the put request.",
								modifying.getPid());
						modifying.abort();
						modifying.setPutError(new PutFailedException("Too long modification. Only " + MAX_MODIFICATION_TIME
								+ "ms are allowed."));
						modifying.notifyPut();
					}
				}
			}

			logger.debug("Queue worker stopped. user id = '{}'", credentials.getUserId());
		}

		/**
		 * Performs a get call (blocking) and decrypts the received user profile.
		 */
		private void get(QueueEntry entry) {
			logger.debug("Get user profile. user id = '{}'", credentials.getUserId());

			DataManager dataManager;
			try {
				dataManager = networkManager.getDataManager();
			} catch (NoPeerConnectionException e) {
				entry.setGetError(new GetFailedException("Node is not connected to the network."));
				return;
			}

			IParameters parameters = new Parameters().setLocationKey(credentials.getProfileLocationKey()).setContentKey(
					H2HConstants.USER_PROFILE);

			// avoid digest call before first get
			if (cachedUserProfile != null) {
				// load the current digest list from network
				NavigableMap<Number640, Number160> digest = dataManager.getDigestLatest(parameters);
				// compare the current user profile's version key with the cached one
				if (digest.firstEntry() != null
						&& digest.firstEntry().getKey().getVersionKey().equals(cachedUserProfile.getVersionKey())) {
					// no need for fetching user profile from network
					entry.setUserProfile(cachedUserProfile);
					return;
				}
			}

			// load latest user profile from network
			NetworkContent content = dataManager.get(parameters);
			if (content == null) {
				logger.warn("Did not find user profile. user id = '{}'", credentials.getUserId());
				entry.setGetError(new GetFailedException("User profile not found. Got null."));
			} else {
				try {
					logger.trace("Decrypting user profile with 256-bit AES key from password. user id = '{}'",
							credentials.getUserId());
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) content;
					NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, userProfileEncryptionKey);
					UserProfile userProfile = (UserProfile) decrypted;
					userProfile.setVersionKey(content.getVersionKey());
					userProfile.setBasedOnKey(content.getBasedOnKey());

					// cache user profile
					cachedUserProfile = userProfile;
					// provide loaded user profile
					entry.setUserProfile(userProfile);
				} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
					logger.error("Cannot decrypt the user profile. reason = '{}'", e.getMessage());
					entry.setGetError(new GetFailedException(String.format("Cannot decrypt the user profile. reason = '%s'",
							e.getMessage())));
				} catch (Exception e) {
					logger.error("Cannot get the user profile. reason = '{}'", e.getMessage());
					entry.setGetError(new GetFailedException(String.format("Cannot get the user profile. reason = '%s'",
							e.getMessage())));
				}
			}
		}

		/**
		 * Encrypts the modified user profile and puts it (blocking).
		 */
		private void put(PutQueueEntry entry) {
			logger.debug("Put user profile. user id = '{}'", credentials.getUserId());
			try {
				logger.trace("Encrypting user profile with 256bit AES key from password. user id ='{}'",
						credentials.getUserId());

				EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(entry.getUserProfile(),
						userProfileEncryptionKey);

				encryptedUserProfile.setBasedOnKey(entry.getUserProfile().getVersionKey());
				encryptedUserProfile.generateVersionKey();

				IParameters parameters = new Parameters().setLocationKey(credentials.getProfileLocationKey())
						.setContentKey(H2HConstants.USER_PROFILE).setVersionKey(encryptedUserProfile.getVersionKey())
						.setData(encryptedUserProfile).setProtectionKeys(entry.getUserProfile().getProtectionKeys())
						.setTTL(entry.getUserProfile().getTimeToLive());

				DataManager dataManager = networkManager.getDataManager();
				boolean success = dataManager.put(parameters);
				if (!success) {
					entry.setPutError(new PutFailedException("Put failed."));
				} else {
					// cache user profile
					cachedUserProfile = entry.getUserProfile();
					cachedUserProfile.setBasedOnKey(encryptedUserProfile.getBasedOnKey());
					cachedUserProfile.setVersionKey(encryptedUserProfile.getVersionKey());
				}
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
				logger.error("Cannot encrypt the user profile. reason = '{}'", e.getMessage());
				entry.setPutError(new PutFailedException(String.format("Cannot encrypt the user profile. reason = '%s'",
						e.getMessage())));
			} catch (NoPeerConnectionException e) {
				entry.setPutError(new PutFailedException("Node is not connected to the network."));
			} finally {
				entry.notifyPut();
			}
		}
	}

	private class QueueEntry {

		private final String pid;

		private final CountDownLatch getWaiter = new CountDownLatch(1);

		private UserProfile userProfile; // got from DHT
		private GetFailedException getFailedException;

		public QueueEntry(String pid) {
			this.pid = pid;
		}

		public String getPid() {
			return pid;
		}

		public void notifyGet() {
			getWaiter.countDown();
		}

		public void waitForGet() throws GetFailedException {
			if (getFailedException != null) {
				throw getFailedException;
			}

			try {
				getWaiter.await();
			} catch (InterruptedException e) {
				getFailedException = new GetFailedException("Could not wait for getting the user profile.");
			}

			if (getFailedException != null) {
				throw getFailedException;
			}
		}

		public void setGetError(GetFailedException error) {
			this.getFailedException = error;
		}

		public GetFailedException getGetError() {
			return getFailedException;
		}

		public UserProfile getUserProfile() {
			return userProfile;
		}

		public void setUserProfile(UserProfile userProfile) {
			this.userProfile = userProfile;
		}

		public boolean equals(String pid) {
			return this.pid.equals(pid);
		}
	}

	private class PutQueueEntry extends QueueEntry {

		private final AtomicBoolean readyToPut = new AtomicBoolean(false);
		private final AtomicBoolean abort = new AtomicBoolean(false);
		private final CountDownLatch putWaiter = new CountDownLatch(1);

		private PutFailedException putFailedException;

		public PutQueueEntry(String pid) {
			super(pid);
		}

		public boolean isReadyToPut() {
			return readyToPut.get();
		}

		public void readyToPut() {
			readyToPut.set(true);
		}

		public boolean isAborted() {
			return abort.get();
		}

		public void abort() {
			abort.set(true);
		}

		public void notifyPut() {
			putWaiter.countDown();
		}

		public void waitForPut() throws PutFailedException {
			if (putFailedException != null) {
				throw putFailedException;
			}

			try {
				putWaiter.await();
			} catch (InterruptedException e) {
				putFailedException = new PutFailedException("Could not wait to put the user profile");
			}

			if (putFailedException != null) {
				throw putFailedException;
			}
		}

		public void setPutError(PutFailedException error) {
			this.putFailedException = error;
		}
	}
}
