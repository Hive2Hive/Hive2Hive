package org.hive2hive.core.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class Index implements Comparable<Index>, Serializable {

	private static final long serialVersionUID = -2643129713985680901L;
	protected final KeyPair fileKeys;
	protected String name;
	protected FolderIndex parent;

	/**
	 * Constructor for root node.
	 * 
	 * @param fileKeys
	 * @param name
	 */
	public Index(KeyPair fileKeys, String name) {
		this.fileKeys = fileKeys;
		this.name = name;
		this.parent = null;
	}

	public Index(KeyPair fileKeys, String name, FolderIndex parent) {
		this.fileKeys = fileKeys;
		this.name = name;
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/**
	 * The {@link MetaDocument} is encrypted with this keypair.
	 * 
	 * @return
	 */
	public KeyPair getFileKeys() {
		return fileKeys;
	}

	/**
	 * Convenience method that returns the public key of the file keys
	 * 
	 * @return
	 */
	public PublicKey getFilePublicKey() {
		return fileKeys.getPublic();
	}

	/**
	 * Returns the name of the file
	 * 
	 * @return the name of the file that this index references
	 */
	public String getName() {
		return name;
	}

	/**
	 * Changes the name of the index. The name is the same as the name of the file.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the parent index (used when the parent may be changed)
	 * 
	 * @param parent
	 */
	public void setParent(FolderIndex parent) {
		if (parent == null) {
			throw new IllegalArgumentException("Parent can't be null.");
		}
		this.parent = parent;
	}

	public void decoupleFromParent() {
		this.parent = null;
	}

	/**
	 * Returns the parent of the index.
	 * 
	 * @return the parent of the current index. This call returns null if the index is root
	 */
	public FolderIndex getParent() {
		return parent;
	}

	/**
	 * Returns the folder that is shared (can be this node or a parent / grand-parent / ... of this node
	 * 
	 * @return
	 */
	public FolderIndex getSharedTopFolder() {
		if (this instanceof FileIndex) {
			// is not shared and is of type files (this has no children)
			return parent.getSharedTopFolder();
		} else {
			// is of type folder
			FolderIndex folder = (FolderIndex) this;
			if (folder.getSharedFlag()) {
				// this is the top-most shared folder because the shared flag is activated
				return folder;
			} else if (folder.isRoot()) {
				// the root folder is never shared
				return null;
			} else {
				// move one level up (recursion)
				return parent.getSharedTopFolder();
			}
		}
	}

	/**
	 * Return sif the node is shared or has any children that are shared.
	 * 
	 * @return if the node is shared or has a shared sub-section
	 */
	public boolean isSharedOrHasSharedChildren() {
		if (isShared()) {
			// this is a shared file or a shared (sub) folder
			return true;
		}

		if (this instanceof FileIndex) {
			// is not shared and is of type 'file'
			return false;
		} else {
			// is of type 'folder', check all subfolders
			List<Index> children = getIndexList(this);
			for (Index child : children) {
				if (child.isFolder()) {
					FolderIndex subfolder = (FolderIndex) child;
					return subfolder.getSharedFlag();
				}
			}
		}

		// no case above matches
		return false;
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

	/**
	 * Returns whether this index belongs to a shared area
	 * 
	 * @return if the index is shared
	 */
	public abstract boolean isShared();

	/**
	 * Returns a list of users that can at least read the file
	 * 
	 * @return
	 */
	public abstract Set<String> getCalculatedUserList();

	/**
	 * Returns the responsible protection keys (depends of the shared state). The {@link MetaDocument} and all
	 * {@link Chunk}s are protected with this key.
	 * 
	 * @return the protection keys (or the default protection keys, set to the root). The result should never
	 *         be null.
	 */
	public abstract KeyPair getProtectionKeys();

	/**
	 * Convenience method to ask whether the index is a folder
	 * 
	 * @return
	 */
	public abstract boolean isFolder();

	/**
	 * Returns whether the user can write and upload a file / sub-folder to this directory
	 * 
	 * @return
	 */
	public abstract boolean canWrite();

	/**
	 * Convenience method to ask whether the index is a file
	 * 
	 * @return
	 */
	public boolean isFile() {
		return !isFolder();
	}

	@Override
	public int compareTo(Index other) {
		return this.getFullPath().compareTo(other.getFullPath());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			String otherPath = (String) obj;
			return getFullPath().equals(otherPath);
		} else if (obj instanceof KeyPair) {
			KeyPair otherKey = (KeyPair) obj;
			return fileKeys.equals(otherKey);
		} else if (obj instanceof Index) {
			Index otherIndex = (Index) obj;
			return fileKeys.equals(otherIndex.getFileKeys());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (fileKeys != null) {
			return fileKeys.hashCode();
		}
		return super.hashCode();
	}

	@Override
	public abstract String toString();

	/**
	 * Walks recursively through the file tree to build, sort and return the whole file list.
	 * 
	 * @param node The root node from which the digest is started.
	 * @return The digest in sorted order.
	 */
	public static List<Path> getFilePathList(Index node) {
		List<Index> fileNodes = getIndexList(node);
		List<Path> digest = new ArrayList<Path>();

		for (Index fileNode : fileNodes) {
			digest.add(fileNode.getFullPath());
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
	public static List<Index> getIndexList(Index node) {
		List<Index> digest = new ArrayList<Index>();

		// add self
		digest.add(node);

		// add children
		if (node.isFolder()) {
			FolderIndex folder = (FolderIndex) node;
			for (Index child : folder.getChildren()) {
				digest.addAll(getIndexList(child));
			}
		}

		return digest;
	}

}
