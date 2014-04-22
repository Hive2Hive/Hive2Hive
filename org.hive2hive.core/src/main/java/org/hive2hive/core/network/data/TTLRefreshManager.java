package org.hive2hive.core.network.data;

import java.util.Timer;
import java.util.TimerTask;

import org.hive2hive.core.H2HConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods to start and stop a periodical task to refresh the time-to-live value of data which has
 * been stored in the network.
 * 
 * @author Seppi
 */
public class TTLRefreshManager {

	private static final Logger logger = LoggerFactory.getLogger(TTLRefreshManager.class);

	private Timer timer;
	private RefreshTask task;

	private final UserProfileManager profileManager;
	private final DataManager dataManager;

	public TTLRefreshManager(UserProfileManager profileManager, DataManager dataManager) {
		this.profileManager = profileManager;
		this.dataManager = dataManager;
	}

	public void start() {
		logger.debug("Starting TTL refresh manager.");

		// create a new timer thread
		timer = new Timer();
		// create a new timer task
		task = new RefreshTask();
		// start refreshment task periodically
		timer.scheduleAtFixedRate(task, H2HConstants.TTL_REFRESHMENT_DELAY,
				H2HConstants.TTL_REFRESHMENT_PERIOD);

		logger.debug("TTL refresh manager started.");
	}

	public void stop() {
		logger.debug("Stopping TTL refresh manager.");

		// cancel the task
		task.cancel();
		// cancel the timer
		timer.cancel();
		// remove all cancelled tasks from this timer's task queu
		timer.purge();

		logger.debug("TTL refresh manager stopped.");
	}

	private class RefreshTask extends TimerTask {

		@Override
		public void run() {
			// // load the user profile
			// UserProfile profile = null;
			// try {
			// String randomPID = UUID.randomUUID().toString();
			// profile = profileManager.getUserProfile(randomPID, false);
			// } catch (GetFailedException e) {
			// logger.error(String.format("Could not get user profile. reason = '%s'", e.getMessage()));
			// return;
			// }
			//
			// // get all file/folder indexes stored in the profile
			// List<Index> indexes = Index.getIndexList(profile.getRoot());
			//
			// // remove all folder indexes
			// Iterator<Index> iterator = indexes.iterator();
			// while (iterator.hasNext()) {
			// Index index = iterator.next();
			//
			// if (index.isFolder())
			// iterator.remove();
			// }
			//
			// // select a random file index
			// Random random = new Random();
			// Index randomIndex = indexes.get(random.nextInt(indexes.size()));
			//
			// // load meta file from network
			// MetaFile metaFile = getMetaFile(randomIndex);
			//
			// for (FileVersion fileVersion : metaFile.getVersions()) {
			// for (String chunkId : fileVersion.getChunkIds()) {
			// NetworkContent content = dataManager.get(chunkId, H2HConstants.FILE_CHUNK);
			// HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			// try {
			// NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, metaFile
			// .getChunkKey().getPrivate());
			// Chunk chunk = (Chunk) decrypted;
			// } catch (ClassNotFoundException | InvalidKeyException | DataLengthException
			// | IllegalBlockSizeException | BadPaddingException | IllegalStateException
			// | InvalidCipherTextException | IllegalArgumentException | IOException e) {
			// logger.error(String.format("Could not decypt chunk. reason = '%s'", e.getMessage()));
			// }
			// }
			// }
		}

		// private MetaFile getMetaFile(Index index) {
		// // get the encrypted meta file
		// KeyPair keyPair = index.getFileKeys();
		// NetworkContent loadedContent = dataManager.get(H2HEncryptionUtil.key2String(keyPair.getPublic()),
		// H2HConstants.META_FILE);
		//
		// // decrypt meta file
		// HybridEncryptedContent encryptedContent = (HybridEncryptedContent) loadedContent;
		// NetworkContent decryptedContent = null;
		// try {
		// decryptedContent = H2HEncryptionUtil.decryptHybrid(encryptedContent, keyPair.getPrivate());
		// } catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
		// | BadPaddingException | IllegalStateException | InvalidCipherTextException
		// | ClassNotFoundException | IOException e) {
		// logger.error(String.format("Could not decrypt meta document. reason = '%s'", e.getMessage()));
		// }
		// MetaFile metaFile = (MetaFile) decryptedContent;
		// //metaFile.setVersionKey(loadedContent.getVersionKey());
		// //metaFile.setBasedOnKey(loadedContent.getBasedOnKey());
		//
		// return metaFile;
		// }

	}

}
