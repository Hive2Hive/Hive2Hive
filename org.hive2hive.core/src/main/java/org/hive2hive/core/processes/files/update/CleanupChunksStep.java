package org.hive2hive.core.processes.files.update;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.context.UpdateFileProcessContext;
import org.hive2hive.core.processes.files.delete.DeleteSingleChunkStep;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes all {@link DeleteSingleChunkStep} to delete the chunks that are not used anymore. These are the
 * ones exceeding the limits at the {@link FileConfiguration}.
 * 
 * @author Nico, Seppi
 */
public class CleanupChunksStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CleanupChunksStep.class);

	private final UpdateFileProcessContext context;
	private final DataManager dataManager;

	public CleanupChunksStep(UpdateFileProcessContext context, DataManager dataManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		List<MetaChunk> chunksToDelete = context.getChunksToDelete();
		KeyPair protectionKeys = context.consumeChunkProtectionKeys();

		logger.debug("Cleaning {} old file chunks.", chunksToDelete.size());
		int counter = 0;
		IProcessComponent<?> prev = this;
		for (MetaChunk metaChunk : chunksToDelete) {
			logger.debug("Delete chunk {} of {}.", counter++, chunksToDelete.size());
			DeleteSingleChunkStep deleteStep = new DeleteSingleChunkStep(metaChunk.getChunkId(), protectionKeys, dataManager);

			// make async, insert it as next step
			IProcessComponent<?> asyncDeletion = new AsyncComponent<>(deleteStep);
			getParent().insertAfter(asyncDeletion, prev);
			prev = asyncDeletion;
		}
		return null;
	}
}
