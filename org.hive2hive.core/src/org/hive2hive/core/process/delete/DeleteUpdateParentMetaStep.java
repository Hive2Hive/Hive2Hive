package org.hive2hive.core.process.delete;

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
 * Updates the parent meta data such that the child is removed from the list
 * 
 * @author Nico, Seppi
 */
public class DeleteUpdateParentMetaStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteUpdateParentMetaStep.class);

	private final String childName;

	public DeleteUpdateParentMetaStep(String childName) {
		this.childName = childName;
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();

		MetaFolder parentMeta = (MetaFolder) context.getParentMetaFolder();
		if (parentMeta == null) {
			getProcess().stop("Parent meta folder is null.");
			return;
		}
		KeyPair parentProtectionKeys = context.getParentProtectionKeys();
		if (parentProtectionKeys == null) {
			getProcess().stop(
					"Content protection keys for parent meta folder are null (no write permission).");
			return;
		}
		MetaDocument childMeta = context.getMetaDocument();
		if (childMeta == null) {
			getProcess().stop("Child meta document is null.");
			return;
		}

		logger.debug(String.format("Removing child meta document of file '%s' from parent meta folder.",
				childMeta.getName()));

		// remove the child from the parent meta data
		parentMeta.removeChildKey(childMeta.getId());

		// notify other clients (can be multiple users)
		DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(parentMeta.getId(),
				childName, parentMeta.getUserList());
		getProcess().sendNotification(messageFactory);

		try {
			HybridEncryptedContent encrypted = H2HEncryptionUtil
					.encryptHybrid(parentMeta, parentMeta.getId());
			encrypted.setBasedOnKey(parentMeta.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(parentMeta.getId()), H2HConstants.META_DOCUMENT, encrypted, parentProtectionKeys);
			getProcess().setNextStep(null); // next step is null, process is done
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Parent meta folder could not be encrypted.");
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
