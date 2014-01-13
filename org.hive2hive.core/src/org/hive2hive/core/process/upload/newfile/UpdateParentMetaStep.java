package org.hive2hive.core.process.upload.newfile;

import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UpdateParentMetaStep extends BasePutProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateParentMetaStep.class);

	public UpdateParentMetaStep() {
		super(new UpdateUserProfileStep());
	}

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		logger.debug(String.format("Start updating the parent meta folder of file: %s", context.getFile()
				.getName()));

		// add child to the parent meta data
		MetaFolder parentMeta = (MetaFolder) context.getMetaDocument();
		if (parentMeta == null) {
			getProcess().stop("Could not find the parent meta data.");
			return;
		}
		KeyPair protectionKeys = (KeyPair) context.getProtectionKeys();
		if (protectionKeys == null) {
			getProcess().stop("No protection keys are set.");
		}

		parentMeta.addChildKeyPair(context.getNewMetaKeyPair());
		logger.debug(String.format("MetaFolder has new child. Total children = ", parentMeta.getChildKeys()
				.size()));

		try {
			logger.debug("Encrypting meta document in a hybrid manner");
			HybridEncryptedContent encrypted = H2HEncryptionUtil
					.encryptHybrid(parentMeta, parentMeta.getId());
			encrypted.setBasedOnKey(parentMeta.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(parentMeta.getId()), H2HConstants.META_DOCUMENT, encrypted, protectionKeys);
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Meta document could not be encrypted");
		}
	}
}
