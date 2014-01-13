package org.hive2hive.core.process.upload.newversion.cleanup;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * Deletes chunks in the DHT. After deleting a chunk, it calls itself recursively until all chunks of all
 * versions are deleted.
 * 
 * @author Nico
 * 
 */
public class DeleteChunkStep extends BaseRemoveProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteChunkStep.class);
	
	private List<KeyPair> chunksToDelete;
	private KeyPair protectionKeys;
	
	DeleteChunkStep(List<KeyPair> chunksToDelete, KeyPair protectionsKeys) {
		super(null);
		this.chunksToDelete = chunksToDelete;
		this.protectionKeys = protectionsKeys;
	}

	@Override
	public void start() {
		if (chunksToDelete.isEmpty()) {
			// done with deleting all chunks
			logger.debug("Finished deleting all chunks of the version to cleanup.");
			getProcess().setNextStep(null);
		} else {
			logger.debug("Cleaning up a old file version. " + chunksToDelete.size()
					+ " more chunks to delete...");
			KeyPair toDelete = chunksToDelete.remove(0);
			nextStep = new DeleteChunkStep(chunksToDelete, protectionKeys);
			// TODO: original chunk is not here in case a rollback happens.
			remove(key2String(toDelete.getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0), protectionKeys);
		}
	}
}
