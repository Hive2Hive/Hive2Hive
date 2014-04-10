package org.hive2hive.core.processes.implementations.share.pkupdate;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.ChunkPKUpdateContext;
import org.hive2hive.core.processes.implementations.context.MetaDocumentPKUpdateContext;

/**
 * Takes the meta file and iteratively changes the protection keys of all chunks.
 * 
 * @author Nico, Seppi
 */
public class InitializeChunkUpdateStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(InitializeChunkUpdateStep.class);

	private final MetaDocumentPKUpdateContext context;
	private final IDataManager dataManager;

	public InitializeChunkUpdateStep(MetaDocumentPKUpdateContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();
		if (metaFile == null) {
			throw new ProcessExecutionException("Meta File not found");
		} else if (!(metaFile instanceof MetaFileSmall)) {
			logger.debug("No need to update any chunks for a large meta file");
			return;
		}

		MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;
		logger.debug(String.format("Initialize updating all chunks for file '%s' in a shared folder.",
				context.getFileName()));
		int counter = 0;
		for (FileVersion version : metaFileSmall.getVersions()) {
			for (MetaChunk metaChunk : version.getMetaChunks()) {
				// each chunk gets an own context
				ChunkPKUpdateContext chunkContext = new ChunkPKUpdateContext(
						context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys(), metaChunk);

				// create the step and wrap it to run asynchronous, attach it to the parent process
				ChangeProtectionKeysStep changeStep = new ChangeProtectionKeysStep(chunkContext, dataManager);
				getParent().add(new AsyncComponent(changeStep));
				counter++;
			}
		}

		logger.debug(String.format("%s chunks of file '%s' need to update their protection keys.", counter,
				context.getFileName()));
	}
}
