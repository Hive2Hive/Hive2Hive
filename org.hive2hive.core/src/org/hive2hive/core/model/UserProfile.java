package org.hive2hive.core.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * File which contains all keys and meta information about the files of the owner.
 * 
 * @author Nico
 * 
 */
public class UserProfile extends NetworkContent {

	private static final long serialVersionUID = 1L;

	private final String userId;
	private final KeyPair encryptionKeys;
	private final KeyPair domainKeys;
	private final FileTreeNode root;

	public UserProfile(String userId, KeyPair encryptionKeys, KeyPair domainKeys) {
		this.userId = userId;
		this.encryptionKeys = encryptionKeys;
		this.domainKeys = domainKeys;
		root = new FileTreeNode(encryptionKeys);
	}

	public String getUserId() {
		return userId;
	}

	public KeyPair getEncryptionKeys() {
		return encryptionKeys;
	}

	public KeyPair getDomainKeys() {
		return domainKeys;
	}

	public FileTreeNode getRoot() {
		return root;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserProfile();
	}

	public static String getLocationKey(UserCredentials credentials) {
		// concatenate PIN + PW + UserId
		String location = new StringBuilder().append(credentials.getPin()).append(credentials.getPassword())
				.append(credentials.getUserId()).toString();

		// create fixed salt based on location
		byte[] fixedSalt = PasswordUtil.generateFixedSalt(location.getBytes());

		// hash the location
		byte[] locationKey = PasswordUtil.generateHash(location.toCharArray(), fixedSalt);

		return new String(locationKey);
	}

	public FileTreeNode getFileById(PublicKey fileId) {
		return findById(root, fileId);
	}

	private FileTreeNode findById(FileTreeNode current, PublicKey fileId) {
		if (current.getKeyPair().getPublic().equals(fileId)) {
			return current;
		}

		FileTreeNode found = null;
		for (FileTreeNode child : current.getChildren()) {
			found = findById(child, fileId);
		}
		return found;
	}

	public FileTreeNode getFileByPath(File file, FileManager fileManager) throws FileNotFoundException {
		String relativePath = file.getAbsolutePath()
				.replaceFirst(fileManager.getRoot().getAbsolutePath(), "");
		return getFileByPath(relativePath);
	}

	public FileTreeNode getFileByPath(String relativePath) throws FileNotFoundException {
		String[] split = relativePath.split(FileManager.FILE_SEP);
		FileTreeNode current = root;
		for (int i = 0; i < split.length; i++) {
			if (split[i].isEmpty()) {
				continue;
			}
			FileTreeNode child = current.getChildByName(split[i]);
			if (child == null) {
				throw new FileNotFoundException("Parent of the file to add does not exist");
			} else {
				current = child;
			}
		}

		return current;
	}
}
