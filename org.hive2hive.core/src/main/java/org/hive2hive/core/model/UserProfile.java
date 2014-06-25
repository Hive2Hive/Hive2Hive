package org.hive2hive.core.model;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * File which contains all keys and meta information about the files of the owner.
 * 
 * @author Nico, Seppi
 * 
 */
public class UserProfile extends NetworkContent {

	private static final long serialVersionUID = -8089242126512434561L;

	private final String userId;
	private final KeyPair encryptionKeys;
	private final FolderIndex root;

	public UserProfile(String userId) {
		assert userId != null;
		this.userId = userId;
		this.encryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);

		// create the root node
		root = new FolderIndex(encryptionKeys, null);
		root.setProtectionKeys(EncryptionUtil.generateRSAKeyPair());
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

	public Index getFileByPath(File file, Path root) {
		Path relativePath = root.relativize(file.toPath());
		return getFileByPath(relativePath);
	}

	public Index getFileByPath(File file, File root) {
		Path relativePath = root.toPath().relativize(file.toPath());
		return getFileByPath(relativePath);
	}

	public Index getFileByPath(Path relativePath) {
		String[] split = relativePath.toString().split(FileUtil.getFileSep());
		FolderIndex current = root;
		for (int i = 0; i < split.length; i++) {
			if (split[i].isEmpty()) {
				continue;
			}
			Index child = current.getChildByName(split[i]);
			if (child == null) {
				return null;
			} else if (child instanceof FolderIndex) {
				current = (FolderIndex) child;
			} else if (child.getFullPath().equals(relativePath)) {
				return child;
			}
		}

		return current;
	}
}
