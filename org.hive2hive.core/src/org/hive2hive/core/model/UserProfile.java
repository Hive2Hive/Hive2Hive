package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserPassword;

/**
 * File which contains all keys and meta information about the files of the owner.
 * 
 * @author Nico
 * 
 */
public class UserProfile extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final KeyPair domainKeys;
	private final String userId;
	private final KeyPair encryptionKeys;
	private FileTreeNode root;

	public UserProfile(String userId, KeyPair encryptionKeys, KeyPair domainKeys) {
		this.userId = userId;
		this.encryptionKeys = encryptionKeys;
		this.domainKeys = domainKeys;
	}

	public KeyPair getDomainKeys() {
		return domainKeys;
	}

	public String getUserId() {
		return userId;
	}

	public KeyPair getEncryptionKeys() {
		return encryptionKeys;
	}

	public FileTreeNode getRoot() {
		return root;
	}

	public void setRoot(FileTreeNode root) {
		this.root = root;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserProfile();
	}

	public String getLocationKey(UserPassword password) {
		return getLocationKey(userId, password);
	}

	public static String getLocationKey(String userId, UserPassword password) {
		// concatenate PIN + PW + UserId
		String location = new StringBuilder().append(password.getPin()).append(password.getPassword())
				.append(userId).toString();

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
}
