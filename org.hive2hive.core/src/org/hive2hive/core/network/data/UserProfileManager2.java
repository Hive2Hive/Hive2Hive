package org.hive2hive.core.network.data;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

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
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Manages the user profile resource. Each process waiting for get / put is added to a queue and delivered in
 * order.
 * 
 * @author Nico
 * 
 */
public class UserProfileManager2 {

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileManager2.class);

	public static final long PUT_GET_AWAIT_TIMEOUT = 10000;
	public static final long MAX_MODIFICATION_TIME = 1000;

	private final NetworkManager networkManager;
	private final UserCredentials credentials;

	private final Queue<QueueEntry> readOnlyQueue;
	private final Queue<PutQueueEntry> modifyQueue;
	private volatile PutQueueEntry modifying;

	public UserProfileManager2(NetworkManager networkManager, UserCredentials credentials) {
		this.networkManager = networkManager;
		this.credentials = credentials;
		readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
		modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();

		new Thread(new QueueWorker()).start();
	}

	public UserCredentials getUserCredentials() {
		return credentials;
	}

	/**
	 * Gets the user profile, if not existent, the call blocks until the most recent profile is here.
	 * 
	 * @param process the process that calls this method
	 * @return never null
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

		entry.waitForGet();
		return entry.getUserProfile();
	}

	/**
	 * Notifies that a process is done with a modification on the user profile.
	 */
	public void stopModification(int pid) {
		// test whether is the current modifying process
		if (modifying != null && modifying.equals(pid)) {
			modifying.abort();
		}
	}

	/**
	 * Waits until the put of the user profile is done. It also assumes that the process is done with the
	 * modifications on the profile.
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

	private class QueueWorker implements Runnable {

		@Override
		public void run() {
			while (true) { // run forever
				// modifying processes have advantage here because the read-only processes can profit
				if (modifyQueue.isEmpty() && readOnlyQueue.isEmpty()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// ignore
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
						readOnlyQueue.poll().notifyGet();
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
						readOnlyQueue.poll().notifyGet();
					}

					int counter = 0;
					long sleepTime = MAX_MODIFICATION_TIME / 10;
					while (counter < 10 && !modifying.isReadyToPut() && !modifying.isAborted()) {
						try {
							counter++;
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							// ignore
						}
					}

					if (modifying.isReadyToPut()) {
						// is ready to put
						logger.debug("Process " + modifying.getPid()
								+ " made modifcations and uploads them now");
						put(modifying);
						logger.debug("Notifying process " + modifying.getPid() + " that putting is finished");
					} else if (!modifying.isAborted()) {
						// request is not ready to put and has not been aborted
						logger.error("Process " + modifying.getPid() + " never finished doing modifications");
						modifying.setPutError(new PutFailedException("Too long modification. Only "
								+ MAX_MODIFICATION_TIME + "ms are allowed."));
					}

					modifying.notifyPut();
				}
			}
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

			FutureGet futureGet = dataManager.getGlobal(credentials.getProfileLocationKey(),
					H2HConstants.USER_PROFILE);
			futureGet.awaitUninterruptibly(PUT_GET_AWAIT_TIMEOUT);

			if (futureGet.isFailed() || futureGet.getData() == null) {
				logger.warn("Did not find user profile.");
				entry.setGetError(new GetFailedException("User profile not found"));
			} else {
				try {
					// decrypt it
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) futureGet.getData()
							.object();

					logger.debug("Decrypting user profile with 256-bit AES key from password.");

					SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(
							credentials.getPassword(), credentials.getPin(), AES_KEYLENGTH.BIT_256);

					NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
					entry.setUserProfile((UserProfile) decrypted);
				} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
						| ClassNotFoundException | IOException e) {
					logger.error("Cannot decrypt the user profile.", e);
					entry.setGetError(new GetFailedException("Cannot decrypt the user profile"));
				}
			}
		}

		/**
		 * Encrypts the modified user profile and puts it (blocking).
		 */
		private void put(PutQueueEntry entry) {
			logger.debug("Encrypting UserProfile with 256bit AES key from password");
			try {
				SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
						credentials.getPin(), AES_KEYLENGTH.BIT_256);
				EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(
						entry.getUserProfile(), encryptionKey);
				logger.debug("Putting UserProfile into the DHT");

				DataManager dataManager = networkManager.getDataManager();
				if (dataManager == null) {
					entry.setPutError(new PutFailedException("Node is not connected to the network"));
					return;
				}

				FuturePut futurePut = dataManager.putGlobal(credentials.getProfileLocationKey(),
						H2HConstants.USER_PROFILE, encryptedUserProfile);
				futurePut.awaitUninterruptibly(PUT_GET_AWAIT_TIMEOUT);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot encrypt the user profile.", e);
				entry.setPutError(new PutFailedException("Cannot encrypt the user profile"));
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
			synchronized (getWaiter) {
				try {
					getWaiter.wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}

			if (getFailedException != null) {
				throw getFailedException;
			}
		}

		public void setGetError(GetFailedException error) {
			this.getFailedException = error;
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
			synchronized (putWaiter) {
				try {
					putWaiter.wait();
				} catch (InterruptedException e) {
					// ignore
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
