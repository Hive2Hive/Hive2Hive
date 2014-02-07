package org.hive2hive.core.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.file.FileManager;

/**
 * Tree implementation for the file tree. It stores the keys for the files and it's logic location.
 * 
 * @author Nico
 * 
 */
public class IndexNode implements Comparable<IndexNode>, Serializable {

	private static final long serialVersionUID = 1L;
	private final KeyPair keyPair;
	private final boolean isFolder;
	private IndexNode parent;
	private String name;
	private byte[] md5LatestVersion;
	private KeyPair protectionKeys = null;
	private final Set<IndexNode> children;
	private boolean isShared = false;

	/**
	 * Constructor for child nodes of type 'folder'
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 * @param isFolder
	 */
	public IndexNode(IndexNode parent, KeyPair keyPair, String name) {
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
	public IndexNode(IndexNode parent, KeyPair keyPair, String name, byte[] md5LatestVersion) {
		this(parent, keyPair, name, false, md5LatestVersion);
	}

	private IndexNode(IndexNode parent, KeyPair keyPair, String name, boolean isFolder,
			byte[] md5LatestVersion) {
		this.parent = parent;
		this.protectionKeys = parent.getProtectionKeys();
		this.keyPair = keyPair;
		this.name = name;
		this.isFolder = isFolder;
		this.md5LatestVersion = md5LatestVersion;
		parent.addChild(this);
		children = new HashSet<IndexNode>();
	}

	/**
	 * Constructor for root node
	 * 
	 * @param keyPair
	 */
	public IndexNode(KeyPair keyPair, KeyPair protectionKeys) {
		this.keyPair = keyPair;
		this.protectionKeys = protectionKeys;
		this.isFolder = true;
		this.parent = null;
		children = new HashSet<IndexNode>();
	}

	/**
	 * Walks recursively through the file tree to build, sort and return the whole file list.
	 * 
	 * @param node The root node from which the digest is started.
	 * @return The digest in sorted order.
	 */
	public static List<Path> getFilePathList(IndexNode node) {
		List<Path> digest = new ArrayList<Path>();

		// add self
		digest.add(node.getFullPath());

		// add children
		for (IndexNode child : node.getChildren()) {
			digest.addAll(getFilePathList(child));
		}

		// sort by full path
		Collections.sort(digest);

		return digest;
	}

	/**
	 * Walks recursively through the file tree and returns a preorder list
	 * 
	 * @param node The root node from which the digest is started.
	 * @return The digest in preorder
	 */
	public static List<IndexNode> getFileNodeList(IndexNode node) {
		List<IndexNode> digest = new ArrayList<IndexNode>();
		// add self
		digest.add(node);

		// add children
		for (IndexNode child : node.getChildren()) {
			digest.addAll(getFileNodeList(child));
		}

		return digest;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public PublicKey getFileKey() {
		return keyPair.getPublic();
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

	public IndexNode getParent() {
		return parent;
	}

	public void setParent(IndexNode parent) {
		this.parent = parent;
	}

	public Set<IndexNode> getChildren() {
		return children;
	}

	public void addChild(IndexNode child) {
		// only add once
		if (getChildByName(child.getName()) == null)
			children.add(child);
	}

	public void removeChild(IndexNode child) {
		if (!children.remove(child)) {
			// remove by name
			children.remove(getChildByName(child.getName()));
		}
	}

	/**
	 * Finds a child with a name. If the child does not exist, null is returned
	 * 
	 * @param name
	 * @return
	 */
	public IndexNode getChildByName(String name) {
		if (name != null) {
			String withoutSeparator = name.replaceAll(FileManager.getFileSep(), "");
			for (IndexNode child : children) {
				if (child.getName().equalsIgnoreCase(withoutSeparator)) {
					return child;
				}
			}
		}
		return null;
	}

	public KeyPair getProtectionKeys() {
		if (!isFolder()) {
			return parent.getProtectionKeys();
		} else {
			return protectionKeys;
		}
	}

	public void setProtectionKeys(KeyPair protectionKeys) {
		if (isRoot())
			throw new IllegalStateException("Not allowed to change root's protection key.");
		else if (!isFolder())
			throw new IllegalStateException(
					"Not allowed to change a file protection key. Only folders hold protection keys.");
		this.protectionKeys = protectionKeys;
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
		if (isShared)
			return true;
		else if (parent == null)
			return false;
		else
			return parent.isShared();
	}

	/**
	 * Returns the folder that is shared (can be this node or a parent / grand-parent / ... of this node
	 * 
	 * @return
	 */
	public IndexNode getSharedTopFolder() {
		if (isShared) {
			return this;
		} else {
			if (isRoot()) {
				return null;
			} else {
				return parent.getSharedTopFolder();
			}
		}
	}

	/**
	 * Sets the share flag. Note that this flag must only be set to the top shared node.
	 */
	public void setIsShared(boolean isShared) throws IllegalStateException {
		if (isRoot() && isShared)
			throw new IllegalStateException("Root node can't be shared.");
		if (!isFolder())
			throw new IllegalStateException("Cannot set a shared flag to a file");

		this.isShared = isShared;
	}

	public boolean isSharedOrHasSharedChildren() {
		if (isShared)
			return true;

		// check for shared children
		for (IndexNode child : children)
			if (child.isSharedOrHasSharedChildren())
				return true;

		// not shared and children not shared either
		return false;
	}

	public boolean canWrite() {
		if (protectionKeys == null) {
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
	public Path getFullPath() {
		if (parent == null) {
			return Paths.get("");
		} else {
			return Paths.get(parent.getFullPath().toString(), getName());
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

	@Override
	public int compareTo(IndexNode other) {
		return this.getFullPath().compareTo(other.getFullPath());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof IndexNode) {
			IndexNode other = (IndexNode) obj;
			return getFileKey().equals(other.getFileKey()) && getName().equalsIgnoreCase(other.getName());
		} else if (obj instanceof PublicKey) {
			PublicKey publicKey = (PublicKey) obj;
			return getFileKey().equals(publicKey);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (keyPair != null)
			return keyPair.hashCode();
		return super.hashCode();
	}
}
