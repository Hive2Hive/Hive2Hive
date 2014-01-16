package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.upload.PutChunkStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Gets the parent meta folder (if file is not in root).
 * 
 * @author Nico, Seppi
 * 
 */
public class GetParentMetaStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(GetParentMetaStep.class);

	private KeyPair parentsKeyPair;

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();

		// get and set the process context
		File file = context.getFile();
		logger.debug("Start getting the parent meta folder of file: " + file.getName());

		File parent = file.getParentFile();

		if (parent.toPath().equals(context.getH2HSession().getFileManager().getRoot())) {
			// no parent to update since the file is in root
			logger.debug("File '" + file.getName()
					+ "' is in root; skip getting the parent meta folder and update the profile directly");
			KeyPair protectionKeys;
			try {
				protectionKeys = context.getH2HSession().getProfileManager().getDefaultProtectionKey();
				context.setProtectionKeys(protectionKeys);
			} catch (GetFailedException e) {
				getProcess().stop(e);
				return;
			}
			getProcess().setNextStep(
					new PutChunkStep(new PutMetaDocumentStep(context.getNewMetaDocument(), protectionKeys,
							new UpdateUserProfileStep())));
		} else {
			// when file is not in root, the parent meta folder must be found
			logger.debug("File '" + file.getName()
					+ "' is not in root; get the meta folder of the parent (lookup in user profile)");

			try {
				UserProfileManager profileManager = context.getH2HSession().getProfileManager();
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
				FileTreeNode parentNode = userProfile.getFileByPath(parent, context.getH2HSession()
						.getFileManager());
				if (parentNode == null) {
					getProcess().stop("Parent file is not in user profile");
					return;
				}
				context.setProtectionKeys(parentNode.getProtectionKeys());

				// initialize the next steps
				// 1. put the new meta document
				// 2. update the parent meta document
				// 3. update the user profile
				parentsKeyPair = parentNode.getKeyPair();
				NetworkContent content = get(key2String(parentNode.getKeyPair().getPublic()),
						H2HConstants.META_DOCUMENT);
				evaluateResult(content);
			} catch (Exception e) {
				getProcess().stop(e);
			}
		}
	}

	public void evaluateResult(NetworkContent content) {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		if (content == null) {
			logger.error("Meta document not found.");
			context.setMetaDocument(null);
			getProcess().stop("Meta document not found.");
			return;
		} else {
			logger.debug("Got encrypted meta document");
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted,
						parentsKeyPair.getPrivate());
				decrypted.setVersionKey(content.getVersionKey());
				decrypted.setBasedOnKey(content.getBasedOnKey());
				context.setMetaDocument((MetaDocument) decrypted);
				logger.debug("Successfully decrypted meta document");
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| IllegalArgumentException e) {
				logger.error("Cannot decrypt the meta document.", e);
				context.setMetaDocument(null);
				getProcess().stop(e);
				return;
			}
		}
		// continue with next step
		getProcess().setNextStep(
				new PutChunkStep(new PutMetaDocumentStep(context.getNewMetaDocument(), context
						.getProtectionKeys(), new UpdateParentMetaStep())));
	}

}
