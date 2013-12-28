package org.hive2hive.core.process.upload.newversion.cleanup;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
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

	private List<KeyPair> chunksToDelete;

	DeleteChunkStep(List<KeyPair> chunksToDelete) {
		super(null);
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public void start() {
		if (chunksToDelete.isEmpty()) {
			// done with deleting all chunks
			getProcess().setNextStep(null);
		} else {
			KeyPair toDelete = chunksToDelete.remove(0);
			nextStep = new DeleteChunkStep(chunksToDelete);
			// TODO: original chunk is not here in case a rollback happens.
			remove(key2String(toDelete.getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0));
		}
	}
}
