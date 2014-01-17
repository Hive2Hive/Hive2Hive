package org.hive2hive.core.process.move;

import java.io.IOException;
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
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which updates the destination parent meta folder of a move process.
 * 
 * @author Nico, Seppi
 */
public class UpdateDestinationParentStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateDestinationParentStep.class);

	@Override
	public void start() {
		logger.debug("Start adding the file to the new parent meta folder.");
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();

		MetaDocument destinationParent = context.getMetaDocument();
		if (destinationParent == null) {
			getProcess().stop("Parent meta folder of destination not found.");
			return;
		}
		KeyPair parentProtectionKeys = context.getDestinationProtectionKeys();
		if (parentProtectionKeys == null) {
			getProcess().stop("Parent meta folder of destination content protection keys not found.");
			return;
		}

		MetaFolder parent = (MetaFolder) destinationParent;
		parent.addChildKeyPair(context.getFileNodeKeys());
		// keep the list of users to notify them about the movement
		context.addUsersToNotifyDestination(parent.getUserList());

		try {
			logger.debug("Encrypting parent meta folder of destination in a hybrid manner.");
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(parent, parent.getId());
			encrypted.setBasedOnKey(parent.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(parent.getId()), H2HConstants.META_DOCUMENT, encrypted, parentProtectionKeys);
			getProcess().setNextStep(new RelinkUserProfileStep());
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Meta folder of destination could not be encrypted.");
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
