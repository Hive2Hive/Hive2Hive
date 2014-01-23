package org.hive2hive.core.process.upload.newversion.cleanup;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * Deletes chunks in the DHT. It iteratively deletes all chunks given in the constructor
 * 
 * @author Nico
 * 
 */
public class DeleteChunkStep extends BaseRemoveProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteChunkStep.class);

	private List<KeyPair> chunksToDelete;
	private KeyPair protectionKeys;

	DeleteChunkStep(List<KeyPair> chunksToDelete, KeyPair protectionsKeys) {
		this.chunksToDelete = chunksToDelete;
		this.protectionKeys = protectionsKeys;
	}

	@Override
	public void start() {
		logger.debug("Cleaning " + chunksToDelete + " old file chunks");
		int counter = 0;
		try {
			for (KeyPair keyPair : chunksToDelete) {
				logger.debug("Delete chunk " + counter++ + "/" + chunksToDelete.size());
				// TODO: original chunk is not here in case a rollback happens.
				remove(key2String(keyPair.getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0),
						protectionKeys);
			}
		} catch (RemoveFailedException e) {
			getProcess().stop(e);
			return;
		}

		// done with deleting all chunks
		logger.debug("Finished deleting all chunks of the version to cleanup.");
		getProcess().setNextStep(null);
	}
}
