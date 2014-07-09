package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
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

	private static final Logger logger = LoggerFactory.getLogger(UserProfileManager.class);
	private static final long MAX_MODIFICATION_TIME = 1000;

	private final UserProfileHolder profileHolder;
	private final UserCredentials credentials;

	private final Object queueWaiter = new Object();
	private final QueueWorker worker = new QueueWorker();
	private final Queue<QueueEntry> readOnlyQueue = new ConcurrentLinkedQueue<QueueEntry>();
	private final Queue<PutQueueEntry> modifyQueue = new ConcurrentLinkedQueue<PutQueueEntry>();

	private volatile PutQueueEntry modifying;

	private final AtomicBoolean running;
	private KeyPair defaultProtectionKey = null;

	public UserProfileManager(DataManager dataManager, UserCredentials credentials) {
		this.credentials = credentials;
		profileHolder = new UserProfileHolder(credentials, dataManager);
		running = new AtomicBoolean(true);

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
			if (intendsToPut) {
				stopModification(pid);
			}
			throw e;
		}

		UserProfile profile = entry.getUserProfile();
		if (profile == null) {
			throw new GetFailedException("User Profile not found");
		}
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
					// a process wants to read
					QueueEntry entry = readOnlyQueue.peek();

					profileHolder.get(entry);

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
					profileHolder.get(modifying);
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
						profileHolder.put(modifying);
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

	}
}
