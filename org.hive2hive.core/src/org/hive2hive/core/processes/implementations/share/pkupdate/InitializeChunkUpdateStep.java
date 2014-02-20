package org.hive2hive.core.processes.implementations.share.pkupdate;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.ChunkPKUpdateContext;
import org.hive2hive.core.processes.implementations.context.MetaDocumentPKUpdateContext;

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
		}

		logger.debug("Initialize updating all chunks for a file in a shared folder...");
		for (FileVersion version : metaFile.getVersions()) {
			for (String chunkId : version.getChunkIds()) {
				// each chunk gets an own context
				ChunkPKUpdateContext chunkContext = new ChunkPKUpdateContext(
						context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys(), chunkId);

				// create the step and wrap it to run asynchronous, attach it to the parent process
				ChangeProtectionKeyStep changeStep = new ChangeProtectionKeyStep(chunkContext, dataManager);
				getParent().add(new AsyncComponent(changeStep));
			}
		}
	}
}
