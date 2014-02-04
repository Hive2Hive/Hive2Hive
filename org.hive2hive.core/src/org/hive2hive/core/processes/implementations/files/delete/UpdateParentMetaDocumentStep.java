package org.hive2hive.core.processes.implementations.files.delete;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

public class UpdateParentMetaDocumentStep extends ProcessStep {

	private final DeleteFileProcessContext context;
	private final NetworkManager networkManager;

	public UpdateParentMetaDocumentStep(DeleteFileProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		if (context.consumeMetaDocument() == null) {
			throw new ProcessExecutionException("No meta document given.");
		}
		
		// get user profile
		UserProfile profile = null;
		try {
			profile = networkManager.getSession().getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException e) {
			throw new ProcessExecutionException("Could not get user profile.", e);
		}
		
		FileTreeNode fileNode = profile.getFileById(context.consumeMetaDocument().getId());
		fileNode.getParent();
		
		// check preconditions
		if (fileNode.getChildren().isEmpty()) {
			throw new ProcessExecutionException("Cannot delete directory that is not empty.");
		}
		
		// update parent
		
	}

}
