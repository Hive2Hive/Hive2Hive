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
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Puts a {@link MetaFile} object into the DHT after encrypting it with the given key.
 * 
 * @author Nico
 * 
 */
public class PutMetaFileStep extends BasePutProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PutMetaFileStep.class);

	private final IConsumeMetaFile metaFileContext;
	private final IConsumeProtectionKeys protectionKeyContext;

	public PutMetaFileStep(IConsumeMetaFile metaFileContext, IConsumeProtectionKeys protectionKeyContext,
			IDataManager dataManager) {
		super(dataManager);
		this.metaFileContext = metaFileContext;
		this.protectionKeyContext = protectionKeyContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			MetaFile metaFile = metaFileContext.consumeMetaFile();
			KeyPair protectionKeys = protectionKeyContext.consumeProtectionKeys();

			logger.debug("Encrypting meta file in a hybrid manner");
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(metaFile, metaFile.getId());
			encrypted.setBasedOnKey(metaFile.getVersionKey());
			encrypted.generateVersionKey();
			put(H2HEncryptionUtil.key2String(metaFile.getId()), H2HConstants.META_FILE, encrypted,
					protectionKeys);
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			throw new ProcessExecutionException("Meta file could not be encrypted.", e);
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}
}
