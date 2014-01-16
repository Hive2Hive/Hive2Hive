package org.hive2hive.core.process.move;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UpdateSourceParentStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateSourceParentStep.class);

	@Override
	public void start() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();

		logger.debug("Start removing the file from the former parent meta folder.");

		MetaDocument sourceParent = context.getMetaDocument();
		if (sourceParent == null) {
			getProcess().stop("Parent meta folder (source) not found.");
			return;
		}
		KeyPair sourceParentProtectionKeys = context.getSourceProtectionKeys();
		if (sourceParentProtectionKeys == null) {
			getProcess().stop("Parent meta folder's (source) content protection keys not found.");
		}

		MetaFolder parent = (MetaFolder) sourceParent;
		PublicKey fileKey = context.getFileNodeKeys().getPublic();
		parent.removeChildKey(fileKey);

		// keep the list of users to notify them about the movement
		context.addUsersToNotifySource(parent.getUserList());

		ProcessStep nextStep;
		if (context.getDestinationParentKeys() == null) {
			logger.debug("No need to update the new parent meta folder since it's moved to root.");
			// file is going to be in root. Next steps:
			// 1. update the user profile
			// 2. notify
			nextStep = new RelinkUserProfileStep();
		} else {
			// initialize next steps:
			// 1. get parent of destination
			// 2. add the new child
			// 3. update the user profile
			// 4. notify
			nextStep = new GetMetaDocumentStep(context.getDestinationParentKeys(),
					new UpdateDestinationParentStep(), context);
		}

		try {
			logger.debug("Encrypting parent meta folder (source) in a hybrid manner.");
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(parent, parent.getId());
			encrypted.setBasedOnKey(parent.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(parent.getId()), H2HConstants.META_DOCUMENT, encrypted, sourceParentProtectionKeys);
			getProcess().setNextStep(nextStep);
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Meta folder of source parent could not be encrypted.");
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
