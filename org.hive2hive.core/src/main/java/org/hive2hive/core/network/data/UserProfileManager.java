package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.vdht.EncryptedVersionManager;
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
	private static final int FORK_LIMIT = 2;

	private final EncryptedVersionManager<UserProfile> versionManager;
	private final UserCredentials credentials;

	private final Object queueWaiter = new Object();
	private final QueueWorker worker = new QueueWorker();
	private final Queue<QueueEntry> readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
	private final Queue<PutQueueEntry> modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();

	private volatile PutQueueEntry modifying;

	private final AtomicBoolean running;
	private KeyPair protectionKeys = null;

	public UserProfileManager(DataManager dataManager, UserCredentials credentials) {
		this.credentials = credentials;
		this.versionManager = new EncryptedVersionManager<UserProfile>(dataManager, PasswordUtil.generateAESKeyFromPassword(
				credentials.getPassword(), credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE),
				credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);
		this.running = new AtomicBoolean(true);

		Thread thread = new Thread(worker);
		thread.setName("UP queue");
		thread.start();
	}

	public void stopQueueWorker() {
		running.set(false);
		synchronized (queueWaiter) {
			queueWaiter.notify();
		}
	}

	public UserCredentials getUserCredentials() {
		return credentials;
	}

	/**
	 * Gets the user profile (read-only). The call blocks until the most recent profile is here.
	 * 
	 * @param pid the process identifier
	 * @return the user profile
	 * @throws GetFailedException if the profile cannot be fetched
	 */
	public UserProfile readUserProfile() throws GetFailedException {
		QueueEntry entry = new QueueEntry();
		readOnlyQueue.add(entry);

		synchronized (queueWaiter) {
			queueWaiter.notify();
		}

		try {
			entry.waitForGet();
		} catch (GetFailedException e) {
			// just stop the modification if an error occurs.
			throw e;
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
	 * @throws AbortModifyException if the modification was aborted
	 */
	public void modifyUserProfile(String pid, IUserProfileModification modifier) throws GetFailedException,
			PutFailedException, AbortModifyException {
		PutQueueEntry entry = new PutQueueEntry(pid);
		modifyQueue.add(entry);

		synchronized (queueWaiter) {
			queueWaiter.notify();
		}

		try {
			entry.waitForGet();
		} catch (GetFailedException e) {
			// just stop the modification if an error occurs.
			stopModification(pid);
			throw e;
		}

		UserProfile profile = entry.getUserProfile();
		if (profile == null) {
			throw new GetFailedException("User Profile not found");
		}

		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			// user starts modifying it
			modifier.modifyUserProfile(profile);

			try {
				// put the updated user profile
				readyToPut(profile, pid);
			} catch (VersionForkAfterPutException e) {
				if (forkCounter++ > FORK_LIMIT) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;

					// retry update of user profile
					continue;
				}
			}

			break;
		}
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
	private void readyToPut(UserProfile profile, String pid) throws PutFailedException {
		if (protectionKeys == null) {
			protectionKeys = profile.getProtectionKeys();
		}

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

	private class QueueWorker implements Runnable {

		@Override
		public void run() {
			// run forever
			while (running.get()) {
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
					try {
						logger.trace("Loading latest version of user profile.");
						UserProfile userProfile = versionManager.get();

						logger.trace("Notifying {} processes that newest profile is ready.", readOnlyQueue.size());
						while (!readOnlyQueue.isEmpty()) {
							QueueEntry readOnly = readOnlyQueue.poll();
							readOnly.setUserProfile(userProfile);
							readOnly.notifyGet();
						}
					} catch (GetFailedException e) {
						logger.warn("Notifying {} processes that getting latest user profile version failed. reason = '{}'",
								readOnlyQueue.size(), e.getMessage());
						while (!readOnlyQueue.isEmpty()) {
							QueueEntry readOnly = readOnlyQueue.poll();
							readOnly.setGetError(e);
							readOnly.notifyGet();
						}
					}
				} else {
					// a process wants to modify
					modifying = modifyQueue.poll();

					logger.trace("Process {} is waiting to make profile modifications.", modifying.getPid());

					try {
						logger.trace("Loading latest version of user profile for process {} to modify.", modifying.getPid());
						modifying.setUserProfile(versionManager.get());
					} catch (GetFailedException e) {
						modifying.setGetError(e);
					}
					modifying.notifyGet();

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
							versionManager.put(modifying.getUserProfile(), protectionKeys);
							modifying.notifyPut();

							// notify all read only processes with newest version
							while (!readOnlyQueue.isEmpty()) {
								QueueEntry readOnly = readOnlyQueue.poll();
								readOnly.setUserProfile(modifying.getUserProfile());
								readOnly.notifyGet();
							}
						} catch (PutFailedException e) {
							modifying.setPutError(e);
							modifying.notifyPut();
						}
					} else if (!modifying.isAborted()) {
						logger.warn("Process {} never finished doing modifications. Abort the put request.",
								modifying.getPid());
						modifying.abort();
						modifying.setPutError(new PutFailedException(String.format(
								"Too long modification. Only %s ms are allowed.", MAX_MODIFICATION_TIME)));
						modifying.notifyPut();
					}
				}
			}

			logger.debug("Queue worker stopped. user id = '{}'", credentials.getUserId());
		}

	}
}
