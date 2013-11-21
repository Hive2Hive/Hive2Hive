package org.hive2hive.core.process.delete;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * Deletes the meta document of the deleted file. After successful deletion, the entry is also removed from
 * the parent meta folder and then from the file tree in the user profile.
 * 
 * @author Nico
 * 
 */
public class DeleteMetaDocumentStep extends BaseRemoveProcessStep {

	public DeleteMetaDocumentStep() {
		super(null);
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		UserProfile userProfile = context.getUserProfile();
		MetaDocument metaDocument = context.getMetaDocument();

		nextStep = new PutUserProfileStep(userProfile, context.getCredentials(), new GetParentMetaStep(
				metaDocument));

		// start the deletion
		remove(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT, metaDocument);
	}
}
