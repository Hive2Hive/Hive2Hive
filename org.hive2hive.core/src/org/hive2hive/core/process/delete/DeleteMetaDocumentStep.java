package org.hive2hive.core.process.delete;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.process.common.remove.RemoveProcessStep;

public class DeleteMetaDocumentStep extends RemoveProcessStep {

	public DeleteMetaDocumentStep() {
		super(null, H2HConstants.META_DOCUMENT, null);
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		UserProfile userProfile = context.getUserProfile();
		MetaDocument metaDocument = context.getMetaDocument();

		// delete node from user profile
		FileTreeNode fileNode = userProfile.getFileById(metaDocument.getId());
		FileTreeNode parent = fileNode.getParent();
		parent.removeChild(fileNode);
		nextStep = new PutUserProfileStep(userProfile, context.getCredentials(), /* TODO notify other clients */
				null);

		// start the deletion
		remove(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT);
	}
}
