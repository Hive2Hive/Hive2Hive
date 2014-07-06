package org.hive2hive.core.processes.share.pkupdate;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.context.ChunkPKUpdateContext;
import org.hive2hive.core.processes.context.MetaDocumentPKUpdateContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the meta file and iteratively changes the protection keys of all chunks.
 * 
 * @author Nico, Seppi
 */
public class InitializeChunkUpdateStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(InitializeChunkUpdateStep.class);

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
		} else if (!(metaFile.isSmall())) {
			logger.debug("No need to update any chunks for a large meta file");
			return;
		}

		MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;
		logger.debug("Initialize updating all chunks for file '{}' in a shared folder.", context.getFileName());
		int counter = 0;
		for (FileVersion version : metaFileSmall.getVersions()) {
			for (MetaChunk metaChunk : version.getMetaChunks()) {
				// each chunk gets an own context
				ChunkPKUpdateContext chunkContext = new ChunkPKUpdateContext(context.consumeOldProtectionKeys(),
						context.consumeNewProtectionKeys(), metaChunk);

				// create the step and wrap it to run asynchronous, attach it to the parent process
				ChangeProtectionKeysStep changeStep = new ChangeProtectionKeysStep(chunkContext, dataManager);
				getParent().add(new AsyncComponent(changeStep));
				counter++;
			}
		}

		logger.debug("{} chunks of file '{}' need to update their protection keys.", counter, context.getFileName());
	}
}
