package org.hive2hive.core.processes.files;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.interfaces.IUploadContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts a {@link MetaFileSmall} object into the DHT after encrypting it with the given key.
 * 
 * @author Nico, Seppi
 */
public class PutMetaFileStep extends BasePutProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutMetaFileStep.class);

	private final IUploadContext context;

	public PutMetaFileStep(IUploadContext context, DataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			BaseMetaFile metaFile = context.consumeMetaFile();
			KeyPair protectionKeys = context.consumeMetaFileProtectionKeys();
			KeyPair encryptionKeys = context.consumeMetaFileEncryptionKeys();

			logger.trace("Encrypting meta file in a hybrid manner.");
			HybridEncryptedContent encrypted = dataManager.getEncryption().encryptHybrid(metaFile,
					encryptionKeys.getPublic());
			encrypted.setBasedOnKey(metaFile.getBasedOnKey());
			encrypted.setVersionKey(metaFile.getVersionKey());
			encrypted.generateVersionKey();

			Parameters parameters = new Parameters().setLocationKey(metaFile.getId()).setContentKey(H2HConstants.META_FILE)
					.setVersionKey(encrypted.getVersionKey()).setBasedOnKey(encrypted.getBasedOnKey())
					.setNetworkContent(encrypted).setProtectionKeys(protectionKeys).setTTL(metaFile.getTimeToLive());

			// data manager has to produce the hash, which gets used for signing
			parameters.setHashFlag(true);
			// put the encrypted meta file into the network
			put(parameters);
			// store the hash
			context.provideMetaFileHash(parameters.getHash());
			setRequiresRollback(true);

		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException ex) {
			throw new ProcessExecutionException(this, ex, "Meta file could not be encrypted.");
		} catch (PutFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		super.doRollback();

		// remove provided hash
		context.provideMetaFileHash(null);
		setRequiresRollback(false);
		return null;
	}
}
