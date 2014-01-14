package org.hive2hive.core.process.delete;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
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

	static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteChunkStep.class);

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

		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			getProcess().stop("No meta document given.");
			return;
		}
		if (context.getProtectionKeys() == null) {
			getProcess().stop("No content protection keys given. User has no write permission.");
			return;
		}

		if (chunksToDelete == null) {
			// first time called, initialize the list
			chunksToDelete = new ArrayList<KeyPair>();
			if (!context.isDirectory()) {
				// no chunks to delete when directory
				MetaFile metaFile = (MetaFile) metaDocument;
				for (FileVersion version : metaFile.getVersions()) {
					chunksToDelete.addAll(version.getChunkIds());
				}
			}
		}

		if (chunksToDelete.isEmpty()) {
			logger.debug(String.format("All chunks of file '%s' deleted.", metaDocument.getName()));
			// continue with next steps:
			// 1. delete the meta document
			// 2. update the parent meta document
			// 2. update the user profile
			// 3. put the updated user profile
			getProcess().setNextStep(new DeleteMetaDocumentStep());
		} else {
			logger.debug(String.format("Deleting file chunks of file = '%s'. %s chunks to remove.",
					metaDocument.getName(), chunksToDelete.size()));
			nextStep = new DeleteChunkStep(chunksToDelete);
			// TODO: original chunk is not here in case a rollback happens.
			remove(key2String(chunksToDelete.remove(0).getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null,
					null, 0, 0), context.getProtectionKeys());
		}
	}
}
