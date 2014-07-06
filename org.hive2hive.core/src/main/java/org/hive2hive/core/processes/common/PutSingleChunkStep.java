package org.hive2hive.core.processes.common;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.interfaces.IPutSingleChunkContext;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts a single chunk without storing it anywhere (thus large files should be no problem).
 * 
 * @author Nico, Seppi
 */
public class PutSingleChunkStep extends BasePutProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutSingleChunkStep.class);

	private final int index;
	private final IPutSingleChunkContext context;
	private final String chunkId;

	public PutSingleChunkStep(IPutSingleChunkContext context, int index, String chunkId, IDataManager dataManager) {
		super(dataManager);
		this.index = index;
		this.context = context;
		this.chunkId = chunkId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		IFileConfiguration config = context.consumeFileConfiguration();

		Chunk chunk;
		try {
			chunk = FileChunkUtil.getChunk(file, config.getChunkSize(), index, chunkId);
		} catch (IOException e) {
			logger.error("File {}: Could not read the file.", file.getAbsolutePath());
			throw new ProcessExecutionException("File " + file.getAbsolutePath() + ": Could not read the file", e);
		}

		if (chunk != null) {
			try {
				// encrypt the chunk prior to put such that nobody can read it
				HybridEncryptedContent encryptedContent = dataManager.getEncryption().encryptHybrid(chunk,
						context.consumeChunkKeys().getPublic());

				logger.debug("Uploading chunk {} of file {}.", chunk.getOrder(), file.getName());
				Parameters parameters = new Parameters().setLocationKey(chunk.getId())
						.setContentKey(H2HConstants.FILE_CHUNK).setData(encryptedContent)
						.setProtectionKeys(context.consumeProtectionKeys()).setTTL(chunk.getTimeToLive());

				// data manager has to produce the hash, which gets used for signing
				parameters.setHashFlag(true);
				// put the encrypted chunk into the network
				put(parameters);

				// store the hash in the index of the meta file
				context.getMetaChunks().add(new MetaChunk(chunkId, parameters.getHash(), index));
			} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException | PutFailedException e) {
				logger.error("Could not encrypt and put the chunk.", e);
				throw new ProcessExecutionException("Could not encrypt and put the chunk.", e);
			}
		}
	}
}
