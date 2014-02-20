package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileUtil;
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
 * @author Nico
 * 
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

		logger.debug("Create chunk keys for the file: " + context.getFile().getName());
		KeyPair chunkKeyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
		context.setChunkEncryptionKeys(chunkKeyPair);

		// only continue if the file has content
		if (file.isDirectory()) {
			logger.debug("File " + file.getName() + ": No data to put because the file is a folder");
			return;
		}

		List<String> chunkIds = new ArrayList<String>();
		int chunks = FileUtil.getNumberOfChunks(file, config.getChunkSize());
		logger.debug(chunks + " chunks to upload for file '" + file.getName() + "'.");
		ProcessComponent prev = this;
		for (int i = 0; i < chunks; i++) {
			String chunkId = UUID.randomUUID().toString();
			chunkIds.add(chunkId);
			PutSingleChunkStep putChunkStep = new PutSingleChunkStep(context, i, chunkId, dataManager, config);

			// insert just after this step
			getParent().insertNext(putChunkStep, prev);
			prev = putChunkStep;
		}

		context.setChunkIds(chunkIds);
	}
}
