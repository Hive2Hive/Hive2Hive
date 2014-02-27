package org.hive2hive.core.processes.implementations.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Gets a {@link MetaFile} from the DHT and decrypts it.
 * 
 * @author Nico
 * 
 */
public class GetMetaFileStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(GetMetaFileStep.class);

	private final IConsumeKeyPair keyContext;
	private final IProvideMetaFile metaContext;

	public GetMetaFileStep(IConsumeKeyPair keyContext, IProvideMetaFile metaContext, IDataManager dataManager) {
		super(dataManager);
		this.keyContext = keyContext;
		this.metaContext = metaContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		KeyPair keyPair = keyContext.consumeKeyPair();
		NetworkContent loadedContent = get(keyPair.getPublic(), H2HConstants.META_FILE);

		if (loadedContent == null) {
			logger.warn("Meta file not found.");
			throw new ProcessExecutionException("Meta file not found.");
		} else {

			// decrypt meta document
			HybridEncryptedContent encryptedContent = (HybridEncryptedContent) loadedContent;

			NetworkContent decryptedContent = null;
			try {
				decryptedContent = H2HEncryptionUtil.decryptHybrid(encryptedContent, keyPair.getPrivate());
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| ClassNotFoundException | IOException e) {
				throw new ProcessExecutionException("Meta file could not be decrypted.", e);
			}

			MetaFile metaFile = (MetaFile) decryptedContent;
			metaFile.setVersionKey(loadedContent.getVersionKey());
			metaFile.setBasedOnKey(loadedContent.getBasedOnKey());

			metaContext.provideMetaFile(metaFile);
			metaContext.provideEncryptedMetaFile(encryptedContent);
			logger.debug("Got and decrypted the meta file.");
		}
	}
}
