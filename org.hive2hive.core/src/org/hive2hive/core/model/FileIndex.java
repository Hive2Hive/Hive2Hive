package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Set;

/**
 * An index stored in the user profile that represents a file in the directory
 * 
 * @author Nico
 * 
 */
public class FileIndex extends Index {

	private static final long serialVersionUID = -465877391037883409L;
	private byte[] md5LatestVersion;

	/**
	 * Constructor for child nodes of type 'file'
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 */
	public FileIndex(FolderIndex parent, KeyPair keyPair, String name, byte[] md5LatestVersion) {
		super(keyPair, name, parent);
		assert parent != null;
		this.md5LatestVersion = md5LatestVersion;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean canWrite() {
		return parent.canWrite();
	}

	public byte[] getMD5() {
		return md5LatestVersion;
	}

	public void setMD5(byte[] md5LatestVersion) {
		this.md5LatestVersion = md5LatestVersion;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FileIndex [");
		sb.append("name=").append(name);
		sb.append(" path=").append(getFullPath()).append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Index) {
			Index other = (Index) obj;
			return getFilePublicKey().equals(other.getFilePublicKey())
					&& getName().equalsIgnoreCase(other.getName());
		} else if (obj instanceof PublicKey) {
			PublicKey publicKey = (PublicKey) obj;
			return getFilePublicKey().equals(publicKey);
		} else {
			return false;
		}
	}

	@Override
	public boolean isShared() {
		return parent.isShared();
	}

	@Override
	public KeyPair getProtectionKeys() {
		return parent.getProtectionKeys();
	}

	@Override
	public Set<String> getCalculatedUserList() {
		return parent.getCalculatedUserList();
	}

}
