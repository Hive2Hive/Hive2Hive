package org.hive2hive.core.processes.files.update;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.context.UpdateFileProcessContext;
import org.hive2hive.core.processes.files.delete.DeleteSingleChunkStep;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes all {@link DeleteSingleChunkStep} to delete the chunks that are not used anymore. These are the
 * ones exceeding the limits at the {@link FileConfiguration}.
 * 
 * @author Nico, Seppi
 */
public class CleanupChunksStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(CleanupChunksStep.class);

	private final UpdateFileProcessContext context;
	private final IDataManager dataManager;

	public CleanupChunksStep(UpdateFileProcessContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		List<MetaChunk> chunksToDelete = context.getChunksToDelete();
		KeyPair protectionKeys = context.consumeProtectionKeys();

		logger.debug("Cleaning {} old file chunks.", chunksToDelete.size());
		int counter = 0;
		ProcessComponent prev = this;
		for (MetaChunk metaChunk : chunksToDelete) {
			logger.debug("Delete chunk {} of {}.", counter++, chunksToDelete.size());
			DeleteSingleChunkStep deleteStep = new DeleteSingleChunkStep(metaChunk.getChunkId(),
					protectionKeys, dataManager);

			// make async, insert it as next step
			AsyncComponent asyncDeletion = new AsyncComponent(deleteStep);
			getParent().insertNext(asyncDeletion, prev);
			prev = asyncDeletion;
		}
	}
}
