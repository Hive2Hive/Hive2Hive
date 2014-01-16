package org.hive2hive.core.process.userprofiletask.share;

import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Adds the key pair of the newly shared folder as a child to the meta folder of the root. All newly added
 * shared folder will be located in the root folder of the logged in user.
 * 
 * @author Seppi
 */
public class UpdateRootMetaStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateRootMetaStep.class);

	@Override
	public void start() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess()
				.getContext();
		logger.debug("Start updating the root meta folder which is parent of the shared folder.");

		MetaFolder rootMeta = (MetaFolder) context.getMetaDocument();
		if (rootMeta == null) {
			getProcess().stop("Could not find the root meta data.");
			return;
		}
		KeyPair protectionKeys = context.getProtectionKeys();
		if (protectionKeys == null) {
			getProcess().stop("Could not find the root meta data's content protection keys.");
			return;
		}

		// add child (shared folder) to the root meta data
		rootMeta.addChildKeyPair(context.getFileTreeNode().getKeyPair());
		logger.debug("MetaFolder of the root has new child (shared folder).");

		try {
			logger.debug("Encrypting meta document in a hybrid manner");
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(rootMeta, rootMeta.getId());
			encrypted.setBasedOnKey(rootMeta.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(rootMeta.getId()), H2HConstants.META_DOCUMENT, encrypted, protectionKeys);
			getProcess().setNextStep(new UpdateUserProfileStep());
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Meta document could not be encrypted");
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
