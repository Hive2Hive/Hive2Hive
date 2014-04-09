package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Puts a single chunk without storing it anywhere (thus large files should be no problem).
 * 
 * @author Nico, Seppi
 */
public class PutSingleChunkStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutSingleChunkStep.class);

	private final int index;
	private final AddFileProcessContext context;
	private final IFileConfiguration config;
	private final String chunkId;

	public PutSingleChunkStep(AddFileProcessContext context, int index, String chunkId,
			IDataManager dataManager, IFileConfiguration config) {
		super(dataManager);
		this.index = index;
		this.context = context;
		this.chunkId = chunkId;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.getFile();

		Chunk chunk;
		try {
			chunk = FileChunkUtil.getChunk(file, config.getChunkSize(), index, chunkId);
		} catch (IOException e) {
			logger.error("File " + file.getAbsolutePath() + ": Could not read the file", e);
			throw new ProcessExecutionException("File " + file.getAbsolutePath()
					+ ": Could not read the file", e);
		}

		if (chunk != null) {
			try {
				// encrypt the chunk prior to put such that nobody can read it
				HybridEncryptedContent encryptedContent = H2HEncryptionUtil.encryptHybrid(chunk, context
						.consumeChunkKeys().getPublic());

				logger.debug("Uploading chunk " + chunk.getOrder() + " of file " + file.getName());
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
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException
					| PutFailedException e) {
				logger.error("Could not encrypt and put the chunk", e);
				throw new ProcessExecutionException("Could not encrypt and put the chunk", e);
			}
		}
	}
}
