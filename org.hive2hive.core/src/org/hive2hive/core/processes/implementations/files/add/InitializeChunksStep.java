package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.security.KeyPair;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Initializes all {@link PutSingleChunkStep} for the file to upload.
 * 
 * @author Nico, Seppi
 */
public class InitializeChunksStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(InitializeChunksStep.class);
	private final AddFileProcessContext context;
	private final IFileConfiguration config;
	private final IDataManager dataManager;

	public InitializeChunksStep(AddFileProcessContext context, IDataManager dataManager,
			IFileConfiguration config) {
		this.context = context;
		this.dataManager = dataManager;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.getFile();

		// only continue if the file has content
		if (file.isDirectory()) {
			logger.trace(String.format("File '%s': No data to put because the file is a folder.",
					file.getName()));
			return;
		} else if (context.isLargeFile()) {
			logger.trace(String.format("File '%s': No data to put because the file is a large file.",
					file.getName()));
			return;
		}

		if (context.consumeChunkKeys() == null) {
			logger.trace(String.format("Create chunk keys for the file '%s'.", file.getName()));
			// create and provide chunk keys
			KeyPair chunkKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
			context.provideChunkKeys(chunkKeys);
		}

		// create put chunks steps
		int chunks = FileChunkUtil.getNumberOfChunks(file, config.getChunkSize());
		logger.trace(String.format("%s chunks to upload for file '%s'.", Integer.toString(chunks),
				file.getName()));
		ProcessComponent prev = this;
		for (int i = 0; i < chunks; i++) {
			String chunkId = UUID.randomUUID().toString();
			PutSingleChunkStep putChunkStep = new PutSingleChunkStep(context, i, chunkId, dataManager, config);

			// insert just after this step
			getParent().insertNext(putChunkStep, prev);
			prev = putChunkStep;
		}
	}

}
