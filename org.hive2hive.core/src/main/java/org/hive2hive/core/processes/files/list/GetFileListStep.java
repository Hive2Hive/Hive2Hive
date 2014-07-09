package org.hive2hive.core.processes.files.list;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processframework.concretes.ResultProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class GetFileListStep extends ResultProcessStep<List<FileTaste>> {

	private final UserProfileManager profileManager;
	private final File rootFile;

	private List<FileTaste> result = new ArrayList<FileTaste>();

	public GetFileListStep(UserProfileManager profileManager, File root) {
		this.profileManager = profileManager;
		this.rootFile = root;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get the user profile
		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("User profile could not be loaded.");
		}

		// the result set
		result.clear();

		// build the digest recursively
		FolderIndex root = profile.getRoot();
		List<Index> digest = Index.getIndexList(root);
		for (Index index : digest) {
			if (index.equals(root)) {
				// skip the root
				continue;
			}

			Path path = index.getFullPath();
			File file = new File(rootFile, path.toString());

			byte[] md5Hash = null;
			Set<UserPermission> userPermissions;
			if (index.isFile()) {
				FileIndex fileIndex = (FileIndex) index;
				md5Hash = fileIndex.getMD5();
				userPermissions = fileIndex.getParent().getCalculatedUserPermissions();
			} else {
				userPermissions = ((FolderIndex) index).getCalculatedUserPermissions();
			}

			result.add(new FileTaste(file, path, md5Hash, userPermissions));
		}

		notifyResultComputed(result);
	}

	@Override
	public List<FileTaste> getResult() {
		return result;
	}
}
