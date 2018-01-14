package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Set;

import org.hive2hive.core.model.versioned.MetaFileSmall;

/**
 * An index stored in the user profile that represents a file in the directory
 * 
 * @author Nico, Seppi
 */
public class FileIndex extends Index {

	private static final long serialVersionUID = -465877391037883409L;
	private byte[] latestVersionHash;
	private byte[] metaFileHash;

	/**
	 * Constructor for child nodes of type 'file'
	 * 
	 * @param parent the parent folder
	 * @param keyPair the file key pair
	 * @param name the name of the file
	 * @param latestVersionHash the hash of the latest version
	 */
	public FileIndex(FolderIndex parent, KeyPair keyPair, String name, byte[] latestVersionHash) {
		super(keyPair, name, parent);
		assert parent != null;
		this.latestVersionHash = latestVersionHash;
	}

	/**
	 * Copy constructor
	 * 
	 * @param fileIndex the index to copy
	 */
	public FileIndex(FileIndex fileIndex) {
		super(fileIndex.fileKeys, fileIndex.name, fileIndex.parent);
		this.latestVersionHash = fileIndex.latestVersionHash;
		this.metaFileHash = fileIndex.metaFileHash;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean canWrite() {
		return parent.canWrite();
	}

	public byte[] getHash() {
		return latestVersionHash;
	}

	public void setHash(byte[] latestVersionHash) {
		this.latestVersionHash = latestVersionHash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FileIndex [");
		sb.append("name=").append(name);
		sb.append(" path=").append(getFullPath()).append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Index) {
			Index other = (Index) obj;
			return getFilePublicKey().equals(other.getFilePublicKey()) && getName().equalsIgnoreCase(other.getName());
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

	/**
	 * Set the hash of the corresponding {@link MetaFileSmall} of this {@link FileIndex}, which gets stored in
	 * the
	 * user profile. The hash gets created while signing the data in front of a put into the network.
	 * 
	 * @param metaFileHash hash of the corresponding {@link MetaFileSmall}
	 */
	public void setMetaFileHash(byte[] metaFileHash) {
		this.metaFileHash = metaFileHash;
	}

	/**
	 * Get the hash of the corresponding {@link MetaFileSmall} of this {@link FileIndex}. The hash gets
	 * created
	 * while signing the {@link MetaFileSmall} in front of a put into the network.
	 * 
	 * @return hash of the corresponding meta file
	 */
	public byte[] getMetaFileHash() {
		return metaFileHash;
	}

}
