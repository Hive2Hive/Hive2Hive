package org.hive2hive.core.model;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * File which contains all keys and meta information about the files of the owner.
 * 
 * @author Nico, Seppi
 * 
 */
public class UserProfile extends NetworkContent {

	private static final long serialVersionUID = 1L;

	private final String userId;
	private final KeyPair encryptionKeys;
	private final FolderIndex root;

	public UserProfile(String userId) {
		this(userId, EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), EncryptionUtil
				.generateProtectionKey());
	}

	/**
	 * Constructor for the user profile.
	 * 
	 * @param userId
	 *            the user id of the owner of the user profile
	 * @param encryptionKeys
	 *            the encryption keys for encrypting the user profile
	 * @param protectionKeys
	 *            the default keys for content protection
	 */
	public UserProfile(String userId, KeyPair encryptionKeys, KeyPair protectionKeys) {
		if (userId == null)
			throw new IllegalArgumentException("User id can't be null.");
		if (encryptionKeys == null)
			throw new IllegalArgumentException("Encryption keys can't be null.");
		if (protectionKeys == null)
			throw new IllegalArgumentException("Protection keys can't be null.");
		this.userId = userId;
		this.encryptionKeys = encryptionKeys;

		// create the root node
		root = new FolderIndex(null, encryptionKeys, null);
		root.setProtectionKeys(protectionKeys);
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

	public Index getFileByPath(File file, FileManager fileManager) {
		Path relativePath = fileManager.getRoot().relativize(file.toPath());
		return getFileByPath(relativePath);
	}

	public Index getFileByPath(File file, File root) {
		Path relativePath = root.toPath().relativize(file.toPath());
		return getFileByPath(relativePath);
	}

	public Index getFileByPath(Path relativePath) {
		String[] split = relativePath.toString().split(FileManager.getFileSep());
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
