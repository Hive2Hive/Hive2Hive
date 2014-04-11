package org.hive2hive.core.processes.implementations.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts a {@link MetaFile} object into the DHT after encrypting it with the given key.
 * 
 * @author Nico, Seppi
 */
public class PutMetaFileStep extends BasePutProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutMetaFileStep.class);

	private final AddFileProcessContext context;

	public PutMetaFileStep(AddFileProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			MetaFile metaFile = context.consumeMetaFile();
			KeyPair protectionKeys = context.consumeProtectionKeys();

			logger.trace("Encrypting meta file of file '{}' in a hybrid manner.", context.getFile().getName());
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(metaFile, metaFile.getId());
			encrypted.setBasedOnKey(metaFile.getVersionKey());
			encrypted.generateVersionKey();

			Parameters parameters = new Parameters()
					.setLocationKey(H2HEncryptionUtil.key2String(metaFile.getId()))
					.setContentKey(H2HConstants.META_FILE).setVersionKey(encrypted.getVersionKey())
					.setData(encrypted).setProtectionKeys(protectionKeys).setTTL(metaFile.getTimeToLive());
			// data manager has to produce the hash, which gets used for signing
			parameters.setHashFlag(true);
			// put the encrypted meta file into the network
			put(parameters);
			// store the hash
			context.provideHash(parameters.getHash());

		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			throw new ProcessExecutionException("Meta file could not be encrypted.", e);
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}
	
	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		super.doRollback(reason);
		
		// remove provided hash
		context.provideHash(null);
	}
}
