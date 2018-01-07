package org.hive2hive.core.processes.files.list;

import java.io.File;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class GetFileListStep extends ProcessStep<FileNode> {

	private final UserProfileManager profileManager;
	private final File rootFile;

	public GetFileListStep(UserProfileManager profileManager, File root) {
		this.profileManager = profileManager;
		this.rootFile = root;
		setName(getClass().getName());
	}

	@Override
	protected FileNode doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get the user profile
		UserProfile profile = null;
		try {
			profile = profileManager.readUserProfile();
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(this, "User profile could not be loaded.");
		}

		// build the digest recursively
		FolderIndex rootIndex = profile.getRoot();
		// FileNode rootNode = new FileNode(null, rootFile, rootIndex.getFullPath(), null,
		// rootIndex.getUserPermissions());
		return copyTree(rootIndex, null);
	}

	private FileNode copyTree(Index current, FileNode parent) {
		if (current == null) {
			return parent;
		}

		String path = current.getFullPath();
		File file = new File(rootFile, path);

		byte[] hash = null;
		Set<UserPermission> userPermissions;
		if (current.isFile()) {
			FileIndex fileIndex = (FileIndex) current;
			hash = fileIndex.getHash();
			userPermissions = fileIndex.getParent().getCalculatedUserPermissions();
		} else {
			userPermissions = ((FolderIndex) current).getCalculatedUserPermissions();
		}

		FileNode node = new FileNode(parent, file, path, hash, userPermissions);
		if (parent != null) {
			parent.getChildren().add(node);
		}

		if (current.isFolder()) {
			// current is a folder, visit recursively
			for (Index child : ((FolderIndex) current).getChildren()) {
				copyTree(child, node);
			}
		}

		return node;
	}
}
