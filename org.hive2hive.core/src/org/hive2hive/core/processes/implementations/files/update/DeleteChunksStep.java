package org.hive2hive.core.processes.implementations.files.update;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStep;
import org.hive2hive.core.processes.implementations.context.UpdateFileProcessContext;

/**
 * Deletes chunks in the DHT. It iteratively deletes all chunks given in the constructor
 * 
 * @author Nico
 * 
 */
public class DeleteChunksStep extends BaseRemoveProcessStep {

	// TODO this class should be merged with
	// org.hive2hive.processes.implementations.files.delete.DeleteChunksProcess

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteChunksStep.class);
	private final UpdateFileProcessContext context;

	public DeleteChunksStep(UpdateFileProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		List<KeyPair> chunksToDelete = context.getChunksToDelete();
		KeyPair protectionKeys = context.getChunksToDeleteProtectionKeys();

		logger.debug("Cleaning " + chunksToDelete.size() + " old file chunks");
		int counter = 0;
		try {
			for (KeyPair keyPair : chunksToDelete) {
				logger.debug("Delete chunk " + counter++ + "/" + chunksToDelete.size());
				// TODO: original chunk is not here in case a rollback happens.
				remove(keyPair.getPublic(), H2HConstants.FILE_CHUNK, new Chunk(null, null, 0, 0),
						protectionKeys);
			}

			// done with deleting all chunks
			logger.debug("Finished deleting all chunks of the version to cleanup.");
		} catch (RemoveFailedException e) {
			throw new ProcessExecutionException(e);
		}

	}
}
