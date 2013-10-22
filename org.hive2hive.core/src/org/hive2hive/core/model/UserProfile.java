package org.hive2hive.core.model;

import java.security.KeyPair;

/**
 * File which contains all keys and meta information about the files of the owner. Every client node tries to
 * have a decrypted up-to-date copy.
 * 
 * @author Nico
 * 
 */
public class UserProfile {

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
}
