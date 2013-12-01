package org.hive2hive.core.process.upload.newfile;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;

/**
 * Gets the parent meta folder (if file is not in root)
 * 
 * @author Nico
 * 
 */
public class GetParentMetaStep extends GetMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetParentMetaStep.class);

	private final MetaDocument childMetaDocument;

	public GetParentMetaStep(MetaDocument childMetaDocument) {
		// TODO this keypair ist just for omitting a NullPointerException at the superclass.
		// There should be a super-constructor not taking any arguments
		super(EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), null, null);
		this.childMetaDocument = childMetaDocument;
	}

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();

		// get and set the process context
		File file = context.getFile();
		logger.debug("Start getting the parent meta folder of file: " + file.getName());

		File parent = file.getParentFile();

		if (parent.equals(context.getFileManager().getRoot())) {
			// no parent to update since the file is in root
			logger.debug("File is in root; skip getting the meta folder and update the profile directly");
			nextStep = new PutMetaDocumentStep(childMetaDocument, new UpdateUserProfileStep());
			getProcess().setNextStep(nextStep);
		} else {
			// normal case when file is not in root
			logger.debug("Get the meta folder of the parent (lookup in user profile)");

			UserProfileManager profileManager = context.getProfileManager();
			UserProfile userProfile = null;
			try {
				userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			} catch (GetFailedException e) {
				getProcess().stop(e.getMessage());
				return;
			}

			FileTreeNode parentNode = userProfile.getFileByPath(parent, context.getFileManager());

			if (parentNode == null) {
				getProcess().stop("Parent file is not in user profile");
				return;
			}

			// initialize the next steps
			// 1. put the new meta document
			// 2. update the parent meta document
			// 3. update the user profile
			nextStep = new PutMetaDocumentStep(childMetaDocument, new UpdateParentMetaStep());

			super.keyPair = parentNode.getKeyPair();
			super.context = context;
			super.get(key2String(parentNode.getKeyPair().getPublic()), H2HConstants.META_DOCUMENT);
		}
	}

}
