package org.hive2hive.core.process.delete;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * Deletes chunks in the DHT. After deleting a chunk, it calls itself recursively until all chunks of all
 * versions are deleted.
 * 
 * @author Nico, Seppi
 * 
 */
public class DeleteChunkStep extends BaseRemoveProcessStep {
	
	private List<KeyPair> chunksToDelete;

	public DeleteChunkStep() {
		super(null);
	}

	private DeleteChunkStep(List<KeyPair> chunksToDelete) {
		super(null);
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		
		if (context.getProtectionKeys() == null) {
			getProcess().stop("No content protection keys given. User has no write permission.");
			return;
		}

		if (chunksToDelete == null) {
			// first time called, initialize the list
			chunksToDelete = new ArrayList<KeyPair>();
			if (!context.isDirectory()) {
				// no chunks to delete when directory
				MetaFile metaFile = (MetaFile) context.getMetaDocument();
				for (FileVersion version : metaFile.getVersions()) {
					chunksToDelete.addAll(version.getChunkIds());
				}
			}
		}

		if (chunksToDelete.isEmpty()) {
			// continue with next steps:
			// 1. delete the meta document
			// 2. update the parent meta document
			// 2. update the user profile
			// 3. put the updated user profile

			getProcess().setNextStep(new DeleteMetaDocumentStep());
		} else {
			KeyPair toDelete = chunksToDelete.remove(0);
			nextStep = new DeleteChunkStep(chunksToDelete);
			// TODO: original chunk is not here in case a rollback happens.
			remove(key2String(toDelete.getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0), context.getProtectionKeys());
		}
	}
}
