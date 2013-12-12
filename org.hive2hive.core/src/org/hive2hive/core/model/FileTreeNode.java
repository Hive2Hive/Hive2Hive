package org.hive2hive.core.model;

import java.io.Serializable;
import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.file.FileManager;

/**
 * Tree implementation for the file tree. It stores the keys for the files and it's logic location.
 * 
 * @author Nico
 * 
 */
public class FileTreeNode implements Serializable {

	private static final long serialVersionUID = 1L;
	private final KeyPair keyPair;
	private final boolean isFolder;
	private FileTreeNode parent;
	private String name;
	private byte[] md5LatestVersion;
	private KeyPair domainKeys;
	private final Set<FileTreeNode> children;

	/**
	 * Constructor for child nodes of type 'folder'
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 * @param isFolder
	 */
	public FileTreeNode(FileTreeNode parent, KeyPair keyPair, String name) {
		this(parent, keyPair, name, true, null);
	}

	/**
	 * Constructor for child nodes of type 'file'
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 * @param isFolder
	 */
	public FileTreeNode(FileTreeNode parent, KeyPair keyPair, String name, byte[] md5LatestVersion) {
		this(parent, keyPair, name, false, md5LatestVersion);
	}

	private FileTreeNode(FileTreeNode parent, KeyPair keyPair, String name, boolean isFolder,
			byte[] md5LatestVersion) {
		this.parent = parent;
		this.keyPair = keyPair;
		this.name = name;
		this.isFolder = isFolder;
		this.setMD5(md5LatestVersion);
		parent.addChild(this);
		children = new HashSet<FileTreeNode>();
	}

	/**
	 * Constructor for root node
	 * 
	 * @param keyPair
	 */
	public FileTreeNode(KeyPair keyPair) {
		this.keyPair = keyPair;
		this.isFolder = true;
		this.parent = null;
		children = new HashSet<FileTreeNode>();
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FileTreeNode getParent() {
		return parent;
	}

	public void setParent(FileTreeNode parent) {
		if (!isRoot())
			this.parent = parent;
	}

	public Set<FileTreeNode> getChildren() {
		return children;
	}

	public void addChild(FileTreeNode child) {
		children.add(child);
	}

	public void removeChild(FileTreeNode child) {
		children.remove(child);
	}

	/**
	 * Finds a child with a name. If the child does not exist, null is returned
	 * 
	 * @param name
	 * @return
	 */
	public FileTreeNode getChildByName(String name) {
		if (name != null) {
			String withoutSeparator = name.replaceAll(FileManager.FILE_SEP, "");
			for (FileTreeNode child : children) {
				if (child.getName().equalsIgnoreCase(withoutSeparator)) {
					return child;
				}
			}
		}
		return null;
	}

	public KeyPair getDomainKeys() {
		return domainKeys;
	}

	public void setDomainKeys(KeyPair domainKeys) {
		if (isFolder) {
			this.domainKeys = domainKeys;
		} else {
			throw new IllegalStateException("The tree object is a file, thus, cannot add a domain key here");
		}
	}

	public byte[] getMD5() {
		return md5LatestVersion;
	}

	public void setMD5(byte[] md5LatestVersion) {
		this.md5LatestVersion = md5LatestVersion;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isShared() {
		if (parent == null) {
			// is root
			return false;
		} else if (isFolder) {
			// TODO: Domain keys indicate if the folder can be written. How to properly indicate that the
			// folder is shared?
			if (domainKeys == null) {
				// no domain keys --> ask parent if shared
				return parent.isShared();
			} else {
				// having domain keys and not beeing root --> shared
				return true;
			}
		} else {
			// ask parent folder
			return parent.isShared();
		}
	}

	public boolean canWrite() {
		if (domainKeys == null) {
			return parent.canWrite();
		} else {
			return true;
		}
	}

	/**
	 * Returns the full path (starting at the root) of this node
	 * 
	 * @return
	 */
	public String getFullPath() {
		if (parent == null) {
			return FileManager.FILE_SEP;
		} else if (isFolder) {
			return parent.getFullPath() + getName() + FileManager.FILE_SEP;
		} else {
			return parent.getFullPath() + getName();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FileTreeNode [");
		sb.append("name=").append(name);
		sb.append(" path=").append(getFullPath());
		sb.append(" isFolder=").append(isFolder);
		sb.append(" children=").append(children.size()).append("]");
		return sb.toString();
	}
}
