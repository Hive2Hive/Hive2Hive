package org.hive2hive.core.process.delete;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * Deletes chunks in the DHT. It iteratively deletes all chunks from the DHT and then continues deleting the
 * meta file itself.
 * 
 * @author Nico, Seppi
 * 
 */
public class DeleteChunkStep extends BaseRemoveProcessStep {

	static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteChunkStep.class);

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

		// initialize the list
		List<KeyPair> chunksToDelete = new ArrayList<KeyPair>();
		if (!context.isDirectory()) {
			// no chunks to delete when directory
			MetaFile metaFile = (MetaFile) metaDocument;
			for (FileVersion version : metaFile.getVersions()) {
				chunksToDelete.addAll(version.getChunkIds());
			}
		}

		int counter = 0;
		try {
			for (KeyPair keyPair : chunksToDelete) {
				logger.debug(String.format("Deleting file chunks of file = '%s'. %s/%s chunks removed.",
						metaDocument.getName(), counter++, chunksToDelete.size()));
				remove(key2String(keyPair.getPublic()), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0),
						context.getProtectionKeys());
			}
		} catch (RemoveFailedException e) {
			logger.error("Could not delete all chunks. We're stuck at " + counter + " of "
					+ chunksToDelete.size() + " chunks.");
			getProcess().stop(e);
			return;
		}

		logger.debug(String.format("All chunks of file '%s' deleted.", metaDocument.getName()));
		// continue with next steps:
		// 1. delete the meta document
		// 2. update the parent meta document
		// 2. update the user profile
		// 3. put the updated user profile
		getProcess().setNextStep(new DeleteMetaDocumentStep());
	}
}
