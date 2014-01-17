package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Manages the user profile resource. Each process waiting for get / put is added to a queue and delivered in
 * order.
 * 
 * @author Nico, Seppi
 * 
 */
public class UserProfileManager {

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileManager.class);
	public static final long MAX_MODIFICATION_TIME = 1000;

	private final NetworkManager networkManager;
	private final UserCredentials credentials;

	private final Object queueWaiter = new Object();

	private final QueueWorker worker;
	private boolean running = true;

	private final Queue<QueueEntry> readOnlyQueue;
	private final Queue<PutQueueEntry> modifyQueue;
	private volatile PutQueueEntry modifying;

	private KeyPair defaultProtectionKey = null;

	public UserProfileManager(NetworkManager networkManager, UserCredentials credentials) {
		this.networkManager = networkManager;
		this.credentials = credentials;
		readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
		modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();

		worker = new QueueWorker();
		new Thread(worker).start();
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
	public UserProfile getUserProfile(int pid, boolean intendsToPut) throws GetFailedException {
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
	public void readyToPut(UserProfile profile, int pid) throws PutFailedException {
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
	private void stopModification(int pid) {
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
			UserProfile userProfile = getUserProfile(0, false);
			defaultProtectionKey = userProfile.getProtectionKeys();
		}
		return defaultProtectionKey;
	}

	private class QueueWorker implements Runnable {

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
					logger.debug(readOnlyQueue.size() + " process(es) are waiting for read-only access");
					// a process wants to read
					QueueEntry entry = readOnlyQueue.peek();

					get(entry);

					logger.debug("Notifying " + readOnlyQueue.size()
							+ " processes that newest profile is ready");
					// notify all read only processes
					while (!readOnlyQueue.isEmpty()) {
						// copy userprofile and errors to other entries
						QueueEntry readOnly = readOnlyQueue.poll();
						readOnly.setUserProfile(entry.getUserProfile());
						readOnly.setGetError(entry.getGetError());
						readOnly.notifyGet();
					}
				} else {
					// a process wants to modify
					modifying = modifyQueue.poll();
					logger.debug("Process " + modifying.getPid()
							+ " is waiting to make profile modifications");
					get(modifying);
					logger.debug("Notifying " + (readOnlyQueue.size() + 1) + " processes (inclusive process "
							+ modifying.getPid() + ") to get newest profile");

					modifying.notifyGet();
					// notify all read only processes
					while (!readOnlyQueue.isEmpty()) {
						// copy userprofile and errors to other entries
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
						logger.debug("Process " + modifying.getPid()
								+ " made modifcations and uploads them now");
						put(modifying);
					} else if (!modifying.isAborted()) {
						// request is not ready to put and has not been aborted
						logger.error("Process " + modifying.getPid()
								+ " never finished doing modifications. Abort the put request.");
						modifying.abort();
						modifying.setPutError(new PutFailedException("Too long modification. Only "
								+ MAX_MODIFICATION_TIME + "ms are allowed."));
						modifying.notifyPut();
					}
				}
			}

			logger.debug("Queue worker stopped.");
		}

		/**
		 * Performs a get call (blocking) and decrypts the received user profile.
		 */
		private void get(QueueEntry entry) {
			logger.debug("Getting the user profile from the DHT");
			DataManager dataManager = networkManager.getDataManager();
			if (dataManager == null) {
				entry.setGetError(new GetFailedException("Node is not connected to the network"));
				return;
			}

			NetworkContent content = dataManager.get(credentials.getProfileLocationKey(),
					H2HConstants.USER_PROFILE);
			entry.processGetResult(content);
		}

		/**
		 * Encrypts the modified user profile and puts it (blocking).
		 */
		private void put(PutQueueEntry entry) {
			logger.debug("Encrypting UserProfile with 256bit AES key from password");
			try {
				SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
						credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);
				EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(
						entry.getUserProfile(), encryptionKey);
				logger.debug("Putting UserProfile into the DHT");

				DataManager dataManager = networkManager.getDataManager();
				if (dataManager == null) {
					entry.setPutError(new PutFailedException("Node is not connected to the network"));
					return;
				}

				encryptedUserProfile.setBasedOnKey(entry.getUserProfile().getVersionKey());
				encryptedUserProfile.generateVersionKey();
				boolean success = dataManager.put(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE,
						encryptedUserProfile, entry.getUserProfile().getProtectionKeys());
				if (!success)
					entry.setPutError(new PutFailedException("Put failed."));
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot encrypt the user profile.", e);
				entry.setPutError(new PutFailedException("Cannot encrypt the user profile"));
			} finally {
				entry.notifyPut();
			}
		}
	}

	private class QueueEntry {
		private final int pid;
		private final Object getWaiter;
		private UserProfile userProfile; // got from DHT
		private GetFailedException getFailedException;

		public QueueEntry(int pid) {
			this.pid = pid;
			this.getWaiter = new Object();
		}

		public int getPid() {
			return pid;
		}

		public void notifyGet() {
			synchronized (getWaiter) {
				getWaiter.notify();
			}
		}

		public void waitForGet() throws GetFailedException {
			if (getFailedException != null) {
				throw getFailedException;
			}

			synchronized (getWaiter) {
				try {
					getWaiter.wait();
				} catch (InterruptedException e) {
					getFailedException = new GetFailedException("Could not wait for getting the user profile");
				}
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

		public boolean equals(int pid) {
			return this.pid == pid;
		}

		public void processGetResult(NetworkContent content) {
			try {
				if (content == null) {
					logger.warn("Did not find user profile.");
					setGetError(new GetFailedException("User profile not found"));
				} else {
					// decrypt it
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) content;

					logger.debug("Decrypting user profile with 256-bit AES key from password.");

					SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(
							credentials.getPassword(), credentials.getPin(),
							H2HConstants.KEYLENGTH_USER_PROFILE);

					NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
					UserProfile userProfile = (UserProfile) decrypted;
					userProfile.setVersionKey(content.getVersionKey());
					userProfile.setBasedOnKey(content.getBasedOnKey());
					setUserProfile(userProfile);
				}
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot decrypt the user profile.", e);
				setGetError(new GetFailedException("Cannot decrypt the user profile"));
			} catch (Exception e) {
				logger.error("Cannot get the user profile. Reason: " + e.getMessage());
				setGetError(new GetFailedException(e.getMessage()));
			}
		}
	}

	private class PutQueueEntry extends QueueEntry {

		private final AtomicBoolean readyToPut;
		private final AtomicBoolean abort;
		private final Object putWaiter;
		private PutFailedException putFailedException;

		public PutQueueEntry(int pid) {
			super(pid);
			putWaiter = new Object();
			readyToPut = new AtomicBoolean(false);
			abort = new AtomicBoolean(false);
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
			synchronized (putWaiter) {
				putWaiter.notify();
			}
		}

		public void waitForPut() throws PutFailedException {
			if (putFailedException != null) {
				throw putFailedException;
			}

			synchronized (putWaiter) {
				try {
					putWaiter.wait();
				} catch (InterruptedException e) {
					putFailedException = new PutFailedException("Could not wait to put the user profile");
				}
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
