package org.hive2hive.core.model;

import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Tree implementation for the file tree. It stores the keys for the files and it's logic location.
 * 
 * @author Nico
 * 
 */
public class FileTreeNode extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final KeyPair keyPair;
	private final boolean isFolder;
	private String name;
	private FileTreeNode parent;
	private KeyPair domainKeys;
	private Set<FileTreeNode> children;

	/**
	 * Constructor for child nodes
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 * @param isFolder
	 */
	public FileTreeNode(FileTreeNode parent, KeyPair keyPair, String name, boolean isFolder) {
		this.parent = parent;
		this.keyPair = keyPair;
		this.name = name;
		this.isFolder = isFolder;
		parent.addChild(this);
		setChildren(new HashSet<FileTreeNode>());
	}

	/**
	 * Constructor for root node
	 * 
	 * @param keyPair
	 */
	public FileTreeNode(KeyPair keyPair) {
		this.keyPair = keyPair;
		this.isFolder = true;
		setChildren(new HashSet<FileTreeNode>());
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
		this.parent = parent;
	}

	public Set<FileTreeNode> getChildren() {
		return children;
	}

	public void setChildren(Set<FileTreeNode> children) {
		this.children = children;
	}

	public void addChild(FileTreeNode child) {
		if (children == null) {
			children = new HashSet<FileTreeNode>();
		}
		children.add(child);
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
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaDocument();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FileTreeNode [");
		sb.append("name=").append(name);
		sb.append(" path=").append(getFullPath());
		sb.append(" isFolder=").append(isFolder);
		sb.append(" children=").append(children.size());
		return sb.toString();
	}
}
