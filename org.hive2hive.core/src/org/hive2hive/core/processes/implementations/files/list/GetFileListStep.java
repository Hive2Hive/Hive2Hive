package org.hive2hive.core.processes.implementations.files.list;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.ResultProcessStep;

public class GetFileListStep extends ResultProcessStep<List<Path>> {

	private final NetworkManager networkManager;

	public GetFileListStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// get the user profile
		UserProfileManager profileManager = null;
		try {
			profileManager = networkManager.getSession().getProfileManager();
		} catch (NoSessionException e) {
			cancel(new RollbackReason(this, "No session found."));
			return;
		}

		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			cancel(new RollbackReason(this, "User profile could not be loaded."));
			return;
		}

		// build the digest recursively
		FolderIndex root = profile.getRoot();
		List<Path> digest = FolderIndex.getFilePathList(root);

		// remove the root
		digest.remove(root.getFullPath());

		notifyResultComputed(digest);
	}
}