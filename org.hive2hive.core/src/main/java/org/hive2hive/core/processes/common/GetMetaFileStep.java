package org.hive2hive.core.processes.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets a {@link MetaFile} from the DHT and decrypts it.
 * 
 * @author Nico
 */
public class GetMetaFileStep extends BaseGetProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(GetMetaFileStep.class);

	private final IGetMetaFileContext context;

	public GetMetaFileStep(IGetMetaFileContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		KeyPair keyPair = context.consumeMetaFileEncryptionKeys();

		NetworkContent loadedContent = get(keyPair.getPublic(), H2HConstants.META_FILE);

		if (loadedContent == null) {
			logger.warn("Meta file not found.");
			throw new ProcessExecutionException("Meta file not found.");
		} else {
			// decrypt meta document
			HybridEncryptedContent encryptedContent = (HybridEncryptedContent) loadedContent;

			NetworkContent decryptedContent = null;
			try {
				decryptedContent = dataManager.getEncryption().decryptHybrid(encryptedContent, keyPair.getPrivate());
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
					| IllegalStateException | InvalidCipherTextException | ClassNotFoundException | IOException e) {
				throw new ProcessExecutionException("Meta file could not be decrypted.", e);
			}

			MetaFile metaFile = (MetaFile) decryptedContent;
			metaFile.setVersionKey(loadedContent.getVersionKey());
			metaFile.setBasedOnKey(loadedContent.getBasedOnKey());

			context.provideMetaFile(metaFile);
			context.provideEncryptedMetaFile(encryptedContent);

			logger.debug("Got and decrypted the meta file.");
		}
	}
}
