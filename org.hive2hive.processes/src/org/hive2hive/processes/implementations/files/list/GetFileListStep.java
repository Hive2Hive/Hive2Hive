package org.hive2hive.processes.implementations.files.list;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.processes.framework.interfaces.IResultProcessComponent;

public class GetFileListStep extends ProcessStep implements IResultProcessComponent<List<Path>> {

	private final List<IProcessResultListener<List<Path>>> listener;
	
	private final NetworkManager networkManager;

	public GetFileListStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
		
		listener = new ArrayList<IProcessResultListener<List<Path>>>();
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
		FileTreeNode root = profile.getRoot();
		List<Path> digest = FileTreeNode.getDigest(root);
		
		notifyResultComputed(digest);
	}

	@Override
	public void notifyResultComputed(List<Path> result) {
		for (IProcessResultListener<List<Path>> listener : this.listener) {
			listener.onResultReady(result);
		}		
	}

	@Override
	public void attachListener(IProcessResultListener<List<Path>> listener) {
		this.listener.add(listener);
	}

	@Override
	public void detachListener(IProcessResultListener<List<Path>> listener) {
		this.listener.remove(listener);
	}
}
