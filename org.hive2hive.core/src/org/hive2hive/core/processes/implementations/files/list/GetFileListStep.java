package org.hive2hive.core.processes.implementations.files.list;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.concretes.ResultProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class GetFileListStep extends ResultProcessStep<List<Path>> {

	private final NetworkManager networkManager;

	public GetFileListStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		// get the user profile
		UserProfileManager profileManager = null;
		try {
			profileManager = networkManager.getSession().getProfileManager();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("User profile could not be loaded.");
		}

		// build the digest recursively
		FolderIndex root = profile.getRoot();
		List<Path> digest = FolderIndex.getFilePathList(root);

		// remove the root
		digest.remove(root.getFullPath());

		notifyResultComputed(digest);
	}
}
