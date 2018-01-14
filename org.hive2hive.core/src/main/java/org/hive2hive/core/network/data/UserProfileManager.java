package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.vdht.AESEncryptedVersionManager;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the user profile resource. Each process waiting for get / put is added to a queue and delivered in
 * order.
 * 
 * @author Nico
 * @author Seppi
 */
public class UserProfileManager {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileManager.class);
	private static final long MAX_MODIFICATION_TIME = 1000;
	private static final long FAILOVER_TIMEOUT = 5 * 60 * 1000;
	private static final int FORK_LIMIT = 2;

	private final AESEncryptedVersionManager<UserProfile> versionManager;
	private final UserCredentials credentials;

	private final Object queueWaiter = new Object();
	private final Queue<QueueEntry> readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
	private final Queue<PutQueueEntry> modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();
	private final AtomicBoolean running = new AtomicBoolean(false);

	private volatile PutQueueEntry modifying;

	private KeyPair protectionKeys = null;
	private Thread workerThread;

	public UserProfileManager(DataManager dataManager, UserCredentials credentials) {
		this.credentials = credentials;

		SecretKey passwordKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(), credentials.getPin(),
				H2HConstants.KEYLENGTH_USER_PROFILE);
		this.versionManager = new AESEncryptedVersionManager<UserProfile>(dataManager, passwordKey,
				credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);
		startQueueWorker();
	}

	public void stopQueueWorker() {
		if (!running.get()) {
			logger.warn("The user profile manager has already been shutdown");
			return;
		}

		running.set(false);

		try {
			// interrupt the thread such that blocking 'wait' calls throw an exception and the thread can
			// shutdown gracefully
			workerThread.checkAccess();
			workerThread.interrupt();
		} catch (SecurityException e) {
			logger.warn("Cannot stop the user profile thread", e);
		}
	}

	public void startQueueWorker() {
		if (running.get()) {
			logger.warn("Queue worker is already running");
		} else {
			running.set(true);
			workerThread = new Thread(new QueueWorker());
			workerThread.setName("UP queue");
			workerThread.start();
		}
	}

	public UserCredentials getUserCredentials() {
		return credentials;
	}

	/**
	 * Gets the user profile (read-only). The call blocks until the most recent profile is here.
	 * 
	 * @return the user profile
	 * @throws GetFailedException if the profile cannot be fetched
	 */
	public UserProfile readUserProfile() throws GetFailedException {
		QueueEntry entry = new QueueEntry();
		readOnlyQueue.add(entry);

		synchronized (queueWaiter) {
			queueWaiter.notify();
		}

		UserProfile profile = entry.getUserProfile();
		if (profile == null) {
			throw new GetFailedException("User Profile not found");
		}
		return profile;
	}

	/**
	 * Gets the user profile and allows to modify it. The call blocks until
	 * {@link IUserProfileModification#modifyUserProfile(UserProfile)} is called or an exception is thrown.
	 * 
	 * @param pid the process identifier
	 * @param modifier the implementation where the modification is done
	 * @throws GetFailedException if the profile cannot be fetched
	 * @throws PutFailedException if the user profile cannot be put
	 * @throws AbortModifyException if the modification was aborted
	 */
	public void modifyUserProfile(String pid, IUserProfileModification modifier)
			throws GetFailedException, PutFailedException, AbortModifyException {
		PutQueueEntry entry = new PutQueueEntry(pid);
		modifyQueue.add(entry);

		synchronized (queueWaiter) {
			queueWaiter.notify();
		}

		UserProfile profile;
		try {
			profile = entry.getUserProfile();
			if (profile == null) {
				throw new GetFailedException("User Profile not found");
			}
		} catch (GetFailedException e) {
			// just stop the modification if an error occurs.
			if (modifying != null && modifying.getPid().equals(pid)) {
				modifying.abort();
			}
			throw e;
		}

		boolean retryPut = true;
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (retryPut) {
			// user starts modifying it
			modifier.modifyUserProfile(profile);

			try {
				// put the updated user profile
				if (protectionKeys == null) {
					protectionKeys = profile.getProtectionKeys();
				}

				if (modifying != null && modifying.getPid().equals(pid)) {
					modifying.setUserProfile(profile);
					modifying.readyToPut();
					modifying.waitForPut();

					// successfully put the user profile
					retryPut = false;
				} else {
					throw new PutFailedException("Not allowed to put anymore");
				}
			} catch (VersionForkAfterPutException e) {
				if (forkCounter++ > FORK_LIMIT) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
					retryPut = false;
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting and retry to update the user profile
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;
				}
			}
		}
	}

	private class QueueWorker implements Runnable {

		@Override
		public void run() {
			// run forever
			while (running.get()) {
				// modifying processes have advantage here because the read-only processes can profit
				if (modifyQueue.isEmpty() && readOnlyQueue.isEmpty()) {
					synchronized (queueWaiter) {
						try {
							// timeout to prevent queues to live forever because of invalid shutdown
							queueWaiter.wait(FAILOVER_TIMEOUT);
						} catch (InterruptedException e) {
							// interrupted, go to next iteration, probably the thread was stopped
							continue;
						}
					}
				} else if (modifyQueue.isEmpty()) {
					logger.trace("{} process(es) are waiting for read-only access.", readOnlyQueue.size());
					try {
						logger.trace("Loading latest version of user profile.");
						UserProfile userProfile = versionManager.get();

						logger.trace("Notifying {} processes that newest profile is ready.", readOnlyQueue.size());
						while (!readOnlyQueue.isEmpty()) {
							QueueEntry readOnly = readOnlyQueue.poll();
							readOnly.setUserProfile(userProfile);
						}
					} catch (GetFailedException e) {
						logger.warn("Notifying {} processes that getting latest user profile version failed. reason = '{}'",
								readOnlyQueue.size(), e.getMessage());
						while (!readOnlyQueue.isEmpty()) {
							QueueEntry readOnly = readOnlyQueue.poll();
							readOnly.setGetError(e);
						}
					}
				} else {
					// a process wants to modify
					modifying = modifyQueue.poll();

					logger.trace("Process {} is waiting to make profile modifications.", modifying.getPid());

					UserProfile userProfile;
					try {
						logger.trace("Loading latest version of user profile for process {} to modify.", modifying.getPid());
						userProfile = versionManager.get();
						modifying.setUserProfile(userProfile);
					} catch (GetFailedException e) {
						modifying.setGetError(e);
						continue;
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
						logger.trace("Process {} made modifcations and uploads them now.", modifying.getPid());
						try {
							// put updated user profile version into network
							versionManager.put(userProfile, protectionKeys);
							modifying.notifyPut();

							// notify all read only processes with newest version
							while (!readOnlyQueue.isEmpty()) {
								QueueEntry readOnly = readOnlyQueue.poll();
								readOnly.setUserProfile(userProfile);
							}
						} catch (PutFailedException e) {
							modifying.setPutError(e);
							modifying.notifyPut();
						}
					} else if (!modifying.isAborted()) {
						logger.warn("Process {} never finished doing modifications. Abort the put request.",
								modifying.getPid());
						modifying.abort();
						modifying.setPutError(new PutFailedException(
								String.format("Too long modification. Only %s ms are allowed.", MAX_MODIFICATION_TIME)));
						modifying.notifyPut();
					}
				}
			}

			logger.debug("Queue worker stopped. user id = '{}'", credentials.getUserId());
		}

	}
}
