package org.hive2hive.core.network.data;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
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
import org.hive2hive.core.process.Process;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Manages the user profile resource. Concurrent processes may make modifications on the user profile. To omit
 * conflicts, they get, modify and put all the same object.
 * 
 * @author Nico
 * 
 */
public class UserProfileManager {

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileManager.class);

	private static final long PUT_GET_HEARTBEAT_MS = 2 * 1000;
	private static final long PUT_GET_AWAIT_TIMEOUT = 5000;
	private static final long MIN_MODIFICATION_TIME_MS = 1000;
	private static final long CONSECUTIVE_ERRORS_ALLOWED = 5;
	private final NetworkManager networkManager;
	private final UserCredentials credentials;

	private final Map<Process, Long> waitingForGet;
	private final Map<Process, Long> waitingForPut;
	private final Map<Process, Long> modifying; // key = process, value = start of modification
	private final PutGetUserProfileTask putGetTask;
	private Phase currentPhase;
	private AtomicBoolean putSuccess = new AtomicBoolean(true);
	private AtomicBoolean getSuccess = new AtomicBoolean(true);

	// multiple threads change this object
	private volatile UserProfile latestUserProfile;

	private enum Phase {
		PUT,
		GET,
		PROCESSING
	}

	public UserProfileManager(NetworkManager networkManager, UserCredentials credentials) {
		this.networkManager = networkManager;
		this.credentials = credentials;
		waitingForGet = new ConcurrentHashMap<Process, Long>();
		waitingForPut = new ConcurrentHashMap<Process, Long>();
		modifying = new ConcurrentHashMap<Process, Long>();
		currentPhase = Phase.GET;

		putGetTask = new PutGetUserProfileTask();
		new Timer("PutGetUserProfile").schedule(putGetTask, 0, PUT_GET_HEARTBEAT_MS);
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
	public UserProfile getUserProfile(Process process) throws GetFailedException {
		// sign in that we want to get the newest profile
		Long waiter = System.currentTimeMillis();
		waitingForGet.put(process, waiter);

		try {
			// wait until the newest profile is here or until a timeout happened
			synchronized (waiter) {
				waiter.wait();
			}
		} catch (InterruptedException e) {
			// ignore
		}

		if (getSuccess.get() && latestUserProfile != null) {
			return latestUserProfile;
		} else {
			throw new GetFailedException("Getting the user profile failed");
		}
	}

	/**
	 * Notifies that a process starts a modification on the user profile. This call is blocking while a
	 * get/put is happening.
	 */
	public void startModification(Process process) {
		// wait until the task is done with processing
		while (currentPhase == Phase.PROCESSING) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		modifying.put(process, System.currentTimeMillis());
	}

	/**
	 * Notifies that a process is done with a modification on the user profile.
	 */
	public void stopModification(Process process) {
		modifying.remove(process);
	}

	/**
	 * Waits until the put of the user profile is done. It also assumes that the process is done with the
	 * modifications on the profile.
	 */
	public void putUserProfile(Process process) throws PutFailedException {
		stopModification(process);

		// sign in that we want to put the newest profile
		Long waiter = System.currentTimeMillis();
		waitingForPut.put(process, waiter);

		try {
			synchronized (waiter) {
				// wait until the newest profile is pushed
				waiter.wait();
			}
		} catch (InterruptedException e) {
			// ignore
		}

		if (!putSuccess.get()) {
			throw new PutFailedException("Putting the user profile failed");
		}
	}

	/**
	 * Puts and gets a user profile with given schedule
	 * 
	 * @author Nico
	 * 
	 */
	private class PutGetUserProfileTask extends TimerTask {

		private int consecutiveGetErrors = 0;
		private int consecutivePutErrors = 0;

		@Override
		public void run() {

			// perform the put or the get
			if (currentPhase == Phase.PUT) {
				currentPhase = Phase.PROCESSING;

				try {
					put();
					consecutivePutErrors = 0; // reset
				} catch (Exception e) {
					consecutivePutErrors++;
					logger.error("Could not put the user profile", e);

					if (consecutivePutErrors > CONSECUTIVE_ERRORS_ALLOWED) {
						// stop all processes waiting
						notifyAll(waitingForPut, false);
						consecutivePutErrors = 0;
					}
				} finally {
					// we putted the latest profile, users are now able to get
					currentPhase = Phase.GET;
				}
			} else {
				currentPhase = Phase.PROCESSING;

				try {
					get();
					consecutiveGetErrors = 0; // reset
				} catch (Exception e) {
					consecutiveGetErrors++;
					logger.error("Could not get the user profile", e);

					if (consecutiveGetErrors > CONSECUTIVE_ERRORS_ALLOWED) {
						// stop all processes waiting
						notifyAll(waitingForGet, false);
						consecutiveGetErrors = 0;
					}
				} finally {
					// the latest profile is fetched, now users are allowed to put
					currentPhase = Phase.PUT;
				}
			}

		}

		/**
		 * Notifies all waiting processes that the task is done
		 */
		private void notifyAll(Map<Process, Long> toNotify, boolean success) {
			logger.debug("Notifying " + toNotify.size() + " process(es) to continue");

			for (Process process : toNotify.keySet()) {
				Long waiter = toNotify.get(process);
				synchronized (waiter) {
					waiter.notify(); // notify
				}
			}

			toNotify.clear();
		}

		/**
		 * Performs a get call (blocking) and decrypts the received user profile.
		 */
		private void get() throws IllegalStateException {
			if (waitingForGet.isEmpty()) {
				// no need to get
				return;
			}

			logger.debug("Getting the user profile from the DHT");
			DataManager dataManager = networkManager.getDataManager();
			if (dataManager == null) {
				getSuccess.set(false);
				throw new IllegalStateException("Node is not connected to the network");
			}

			FutureGet futureGet = dataManager.getGlobal(credentials.getProfileLocationKey(),
					H2HConstants.USER_PROFILE);
			futureGet.awaitUninterruptibly(PUT_GET_AWAIT_TIMEOUT);

			if (futureGet.isFailed() || futureGet.getData() == null) {
				logger.warn("Did not find user profile.");
				latestUserProfile = null;
				getSuccess.set(false);
				notifyAll(waitingForGet, false);
			} else {
				try {
					// decrypt it
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) futureGet.getData()
							.object();

					logger.debug("Decrypting user profile with 256-bit AES key from password.");

					SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(
							credentials.getPassword(), credentials.getPin(), AES_KEYLENGTH.BIT_256);

					NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
					latestUserProfile = (UserProfile) decrypted;
					getSuccess.set(true);
					notifyAll(waitingForGet, true);
				} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
						| ClassNotFoundException | IOException e) {
					logger.error("Cannot decrypt the user profile.", e);
					getSuccess.set(false);
					notifyAll(waitingForGet, false);
				}
			}
		}

		/**
		 * Encrypts the modified user profile and puts it (blocking).
		 */
		private void put() throws IllegalStateException {
			if (waitingForPut.isEmpty()) {
				// no need to put
				return;
			}

			waitForModifyingProcesses();

			logger.debug("Encrypting UserProfile with 256bit AES key from password");
			try {
				SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
						credentials.getPin(), AES_KEYLENGTH.BIT_256);
				EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(
						latestUserProfile, encryptionKey);
				logger.debug("Putting UserProfile into the DHT");

				DataManager dataManager = networkManager.getDataManager();
				if (dataManager == null) {
					throw new IllegalStateException("Node is not connected to the network");
				}

				FuturePut futurePut = dataManager.putGlobal(credentials.getProfileLocationKey(),
						H2HConstants.USER_PROFILE, encryptedUserProfile);
				futurePut.awaitUninterruptibly(PUT_GET_AWAIT_TIMEOUT);

				putSuccess.set(futurePut.isSuccess());
				notifyAll(waitingForPut, futurePut.isSuccess());
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot encrypt the user profile.", e);
				putSuccess.set(false);
				notifyAll(waitingForPut, false);
			}
		}

		/**
		 * Some processes could still be modifying that user profile. Give them an extra time to finish.
		 */
		private void waitForModifyingProcesses() {
			if (!modifying.isEmpty()) {
				try {
					logger.info(modifying.size()
							+ " processes are still modifying the profile. Wait for another "
							+ MIN_MODIFICATION_TIME_MS + "ms");
					Thread.sleep(MIN_MODIFICATION_TIME_MS);
				} catch (InterruptedException e) {
					// ignore
				}
			}

			if (!modifying.isEmpty()) {
				// TODO kill processes from this thread?
				logger.info(modifying.size() + " processes are still modifying the profile. Quit them.");
				for (Process process : modifying.keySet()) {
					process.stop("Profile modificatin was too long");
				}
				modifying.clear();
			}
		}
	}
}
