package org.hive2hive.processes.implementations.files.delete;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.DeleteFileProcessContext;

public class UpdateParentMetaDocumentStep extends ProcessStep {

	private final DeleteFileProcessContext context;
	private final NetworkManager networkManager;

	public UpdateParentMetaDocumentStep(DeleteFileProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "No meta document given."));
			return;
		}
		
		// get user profile
		UserProfile profile = null;
		try {
			profile = networkManager.getSession().getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException e) {
			cancel(new RollbackReason(this, "Could not get user profile."));
			return;
		}
		
		FileTreeNode fileNode = profile.getFileById(context.consumeMetaDocument().getId());
		fileNode.getParent();
		
		// check preconditions
		if (fileNode.getChildren().isEmpty()) {
			cancel(new RollbackReason(this, "Cannot delete directory that is not empty."));
			return;
		}
		
		// update parent
		
	}

}
