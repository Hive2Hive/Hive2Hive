package org.hive2hive.core.network.data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
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

	private static final long MAX_DELAY_MS = 15 * 1000;
	private static final long MIN_MODIFICATION_TIME_MS = 2000;
	private final NetworkManager networkManager;
	private final UserCredentials credentials;

	private final Map<Process, Long> waitingForGet;
	private final Map<Process, Long> waitingForPut;
	private final Map<Process, Long> modifying; // key = process, value = start of modification
	private final PutGetUserProfileTask putGetTask;

	// multiple threads change this object
	private volatile UserProfile latestUserProfile;

	private Phase currentPhase;

	private enum Phase {
		ACCEPT_PUT,
		ACCEPT_GET,
		PROCESSING
	}

	public UserProfileManager(NetworkManager networkManager, UserCredentials credentials) {
		this.networkManager = networkManager;
		this.credentials = credentials;
		waitingForGet = new ConcurrentHashMap<Process, Long>();
		waitingForPut = new ConcurrentHashMap<Process, Long>();
		modifying = new ConcurrentHashMap<Process, Long>();
		currentPhase = Phase.ACCEPT_GET;

		putGetTask = new PutGetUserProfileTask();
		new Thread(putGetTask).start();
	}

	/**
	 * Gets the user profile, if not existent, the call blocks until the most recent profile is here.
	 */
	public UserProfile getUserProfile(Process process) {
		// wait until the task accepts get requests
		while (currentPhase != Phase.ACCEPT_GET) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		// sign in that we want to get the newest profile
		long now = System.currentTimeMillis();
		waitingForGet.put(process, System.currentTimeMillis());

		// wait until the newest profile is here
		while (now > putGetTask.lastGetTime.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		return latestUserProfile;
	}

	/**
	 * Notifies that a process starts a modification on the user profile. This call is blocking while a
	 * get/put is happening.
	 */
	public void startModification(Process process) {
		// wait until the task is done with processing
		while (currentPhase == Phase.PROCESSING) {
			try {
				Thread.sleep(1000);
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
	public void putUserProfile(Process process) {
		stopModification(process);

		// wait until the task accepts put requests
		while (currentPhase != Phase.ACCEPT_PUT) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		// sign in that we want to put the newest profile
		long now = System.currentTimeMillis();
		waitingForPut.put(process, System.currentTimeMillis());

		// wait until the newest profile is pushed
		while (now > putGetTask.lastPutTime.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	/**
	 * Puts and gets a user profile with given schedule (running forever)
	 * 
	 * @author Nico
	 * 
	 */
	private class PutGetUserProfileTask implements Runnable {

		public final AtomicLong lastGetTime;
		public final AtomicLong lastPutTime;

		public PutGetUserProfileTask() {
			lastGetTime = new AtomicLong(-1);
			lastPutTime = new AtomicLong(-1);
		}

		@Override
		public void run() {
			while (true) {
				try {
					if (currentPhase == Phase.ACCEPT_GET && waitingForGet.isEmpty()
							&& !waitingForPut.isEmpty()) {
						// someone is waiting to put --> switch faster
						Thread.sleep(MAX_DELAY_MS / 5);
					} else if (currentPhase == Phase.ACCEPT_PUT && waitingForPut.isEmpty()
							&& !waitingForGet.isEmpty()) {
						// someone is waiting to get --> switch faster
						Thread.sleep(MAX_DELAY_MS / 5);
					} else {
						// sleep a given time such that processes have time to modify the user profile
						Thread.sleep(MAX_DELAY_MS - MIN_MODIFICATION_TIME_MS);
					}
				} catch (InterruptedException e) {
					// ignore
				}

				// then perform the put or the get
				if (currentPhase == Phase.ACCEPT_PUT) {
					currentPhase = Phase.PROCESSING;
					put();
					// we putted the latest profile, users are now able to get
					currentPhase = Phase.ACCEPT_GET;
				} else {
					currentPhase = Phase.PROCESSING;
					get();
					// the latest profile is fetched, now users are allowed to put
					currentPhase = Phase.ACCEPT_PUT;
				}
			}
		}

		private void get() {
			if (waitingForGet.isEmpty()) {
				// no need to get
				return;
			}

			logger.debug("Getting the user profile from the DHT");
			FutureGet future = networkManager.getGlobal(credentials.getProfileLocationKey(),
					H2HConstants.USER_PROFILE);
			future.awaitUninterruptibly();

			if (future.getData() == null) {
				logger.warn("Did not find user profile.");
				latestUserProfile = null;
			} else {
				try {
					// decrypt it
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) future.getData().object();
					logger.debug("Decrypting user profile with 256-bit AES key from password.");

					SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(
							credentials.getPassword(), credentials.getPin(), AES_KEYLENGTH.BIT_256);

					NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
					latestUserProfile = (UserProfile) decrypted;
					onGetSuccess();
				} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
						| ClassNotFoundException | IOException e) {
					logger.error("Cannot decrypt the user profile.", e);
					onGetFail();
				}
			}
		}

		private void onGetFail() {
			// must have been a conflict
			// trigger rollback of the processes that depend on the current get
			for (Process process : waitingForGet.keySet()) {
				process.stop("Getting the user profile failed");
			}

			// clean the map
			waitingForGet.clear();
		}

		private void onGetSuccess() {
			// successful, processes depending on current get can continue
			lastGetTime.set(System.currentTimeMillis());

			// clean the map
			waitingForGet.clear();
		}

		private void put() {
			if (waitingForPut.isEmpty()) {
				// no need to put
				return;
			}

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
				logger.info(modifying.size() + " processes are still modifying the profile. Quit them.");
				for (Process process : modifying.keySet()) {
					process.stop("Profile modificatin was too long");
				}
				modifying.clear();
			}

			logger.debug("Encrypting UserProfile with 256bit AES key from password");
			try {
				SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
						credentials.getPin(), AES_KEYLENGTH.BIT_256);
				EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(
						latestUserProfile, encryptionKey);
				logger.debug("Putting UserProfile into the DHT");

				networkManager.putGlobal(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE,
						encryptedUserProfile).awaitUninterruptibly();
				onPutSuccess();
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot encrypt the user profile.", e);
				onPutFail();
			}
		}

		private void onPutFail() {
			// must have been a conflict
			// trigger rollback of the processes that depend on the current put
			for (Process process : waitingForPut.keySet()) {
				process.stop("Putting the user profile failed");
			}

			// clean the map
			waitingForPut.clear();
		}

		private void onPutSuccess() {
			// successful, processes depending on current put can continue
			lastPutTime.set(System.currentTimeMillis());

			// clean the map
			waitingForPut.clear();
		}
	}
}
