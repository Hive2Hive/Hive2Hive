package org.hive2hive.core.model;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree implementation for the file tree. It stores the keys for the files and it's logic location.
 * 
 * @author Nico
 * 
 */
public class FileTreeNode {

	private final KeyPair keyPair;
	private final boolean isFolder;
	private String name;
	private FileTreeNode parent;
	private KeyPair domainKeys;
	private List<FileTreeNode> children;

	public FileTreeNode(FileTreeNode parent, KeyPair keyPair, String name, boolean isFolder) {
		this.parent = parent;
		this.keyPair = keyPair;
		this.name = name;
		this.isFolder = isFolder;
		setChildren(new ArrayList<FileTreeNode>());
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

	public List<FileTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<FileTreeNode> children) {
		this.children = children;
	}

	public void addChild(FileTreeNode child) {
		if (children == null) {
			children = new ArrayList<FileTreeNode>();
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
		// TODO: go down recursively
		return false;
	}

	public boolean canWrite() {
		if (domainKeys == null) {
			return parent.canWrite();
		} else {
			return true;
		}
	}

	public String getFullPath() {
		return parent.getFullPath() + File.pathSeparator + getName();
	}
}
