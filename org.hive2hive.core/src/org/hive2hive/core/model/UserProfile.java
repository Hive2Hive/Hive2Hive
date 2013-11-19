package org.hive2hive.core.model;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.NetworkContent;

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
			if (found != null) {
				return found;
			}
		}
		return found;
	}

	public FileTreeNode getFileByPath(File file, FileManager fileManager) {
		String relativePath = file.getAbsolutePath()
				.replaceFirst(fileManager.getRoot().getAbsolutePath(), "");
		return getFileByPath(relativePath);
	}

	public FileTreeNode getFileByPath(String relativePath) {
		String[] split = relativePath.split(FileManager.FILE_SEP);
		FileTreeNode current = root;
		for (int i = 0; i < split.length; i++) {
			if (split[i].isEmpty()) {
				continue;
			}
			FileTreeNode child = current.getChildByName(split[i]);
			if (child == null) {
				return null;
			} else {
				current = child;
			}
		}

		return current;
	}
}
