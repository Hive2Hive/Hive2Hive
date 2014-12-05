package org.hive2hive.core.model.versioned;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * File which contains all keys and meta information about the files of the owner.
 * 
 * @author Nico, Seppi
 * 
 */
public class UserProfile extends BaseVersionedNetworkContent {

	private static final long serialVersionUID = -8089242126512434561L;

	private final String userId;
	private final KeyPair encryptionKeys;
	private final FolderIndex root;

	public UserProfile(String userId) {
		assert userId != null;
		this.userId = userId;
		this.encryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);

		// create the root node
		root = new FolderIndex(encryptionKeys);
		root.setProtectionKeys(EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION));
		root.addUserPermissions(new UserPermission(userId, PermissionType.WRITE));
	}

	public String getUserId() {
		return userId;
	}

	public KeyPair getEncryptionKeys() {
		return encryptionKeys;
	}

	public KeyPair getProtectionKeys() {
		return root.getProtectionKeys();
	}

	public FolderIndex getRoot() {
		return root;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserProfile();
	}

	public Index getFileById(PublicKey fileId) {
		return findById(root, fileId);
	}

	private Index findById(Index current, PublicKey fileId) {
		if (current.getFilePublicKey().equals(fileId)) {
			return current;
		}

		Index found = null;
		if (current instanceof FolderIndex) {
			FolderIndex folder = (FolderIndex) current;
			for (Index child : folder.getChildren()) {
				found = findById(child, fileId);
				if (found != null) {
					return found;
				}
			}
		}

		return found;
	}

	public Index getFileByPath(File file, File root) {
		// holds all files in-order
		File currentFile = new File(file.getAbsolutePath());
		List<String> filePath = new ArrayList<String>();
		while (!root.equals(currentFile) && currentFile != null) {
			filePath.add(currentFile.getName());
			currentFile = currentFile.getParentFile();
		}
		Collections.reverse(filePath);

		FolderIndex currentIndex = this.root;
		for (String fileName : filePath) {
			Index child = currentIndex.getChildByName(fileName);
			if (child == null) {
				return null;
			} else if (child instanceof FolderIndex) {
				currentIndex = (FolderIndex) child;
			} else if (child.getName().equals(file.getName())) {
				return child;
			}
		}
		return currentIndex;
	}
}
