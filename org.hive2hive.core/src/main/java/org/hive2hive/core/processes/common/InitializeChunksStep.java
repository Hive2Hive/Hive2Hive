package org.hive2hive.core.processes.common;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.context.interfaces.IInitializeChunksStepContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes all {@link PutSingleChunkStep} for the file to upload.
 * 
 * @author Nico, Seppi
 */
public class InitializeChunksStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(InitializeChunksStep.class);

	private final IInitializeChunksStepContext context;
	private final IDataManager dataManager;

	public InitializeChunksStep(IInitializeChunksStepContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();

		// only continue if the file has content
		if (file.isDirectory()) {
			logger.trace("File '{}': No data to put because the file is a folder.", file.getName());
			return;
		} else if (context.isLargeFile()) {
			initLargeFile(file);
		} else {
			initSmallFile(file);
		}
	}

	private void initSmallFile(File file) {
		if (context.consumeChunkKeys() == null) {
			logger.trace("Create chunk keys for the file '{}'.", file.getName());
			// create and provide chunk keys
			KeyPair chunkKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
			context.provideChunkKeys(chunkKeys);
		}

		// create put chunks steps
		IFileConfiguration config = context.consumeFileConfiguration();
		int chunks = FileChunkUtil.getNumberOfChunks(file, config.getChunkSize());
		logger.trace("{} chunks to upload for file '{}'.", Integer.toString(chunks), file.getName());
		ProcessComponent prev = this;
		for (int i = 0; i < chunks; i++) {
			String chunkId = UUID.randomUUID().toString();
			PutSingleChunkStep putChunkStep = new PutSingleChunkStep(context, i, chunkId, dataManager);

			// insert just after this step
			getParent().insertNext(putChunkStep, prev);
			prev = putChunkStep;
		}
	}

	private void initLargeFile(File file) throws ProcessExecutionException {
		// init the large file chunks
		IFileConfiguration config = context.consumeFileConfiguration();
		int chunks = FileChunkUtil.getNumberOfChunks(file, config.getChunkSize());
		logger.trace(String.format("%s chunks for large file '%s'.", Integer.toString(chunks), file.getName()));

		// process chunk for chunk, hash it and add the meta information to the context
		for (int i = 0; i < chunks; i++) {
			String chunkId = UUID.randomUUID().toString();
			Chunk chunk;

			try {
				chunk = FileChunkUtil.getChunk(file, config.getChunkSize(), i, chunkId);
			} catch (IOException e) {
				throw new ProcessExecutionException("Cannot read the large file", e);
			}

			byte[] md5Hash = HashUtil.hash(chunk.getData());
			context.getMetaChunks().add(new MetaChunk(chunkId, md5Hash, i));
		}
	}
}
