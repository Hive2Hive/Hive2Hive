package org.hive2hive.core.model;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.versioned.MetaFileLarge;
import org.hive2hive.core.model.versioned.MetaFileSmall;

public abstract class Index implements Comparable<Index>, Serializable {

	private static final long serialVersionUID = -2643129713985680901L;
	protected final KeyPair fileKeys;
	protected String name;
	protected FolderIndex parent;

	/**
	 * Constructor for root node.
	 * 
	 * @param fileKeys the root file keys
	 */
	public Index(KeyPair fileKeys) {
		this(fileKeys, null, null);
	}

	public Index(KeyPair fileKeys, String name, FolderIndex parent) {
		if (fileKeys == null) {
			throw new IllegalArgumentException("File keys can't be null.");
		}
		this.fileKeys = fileKeys;
		this.name = name;
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/**
	 * The {@link MetaFileSmall} or {@link MetaFileLarge} is encrypted with this keypair.
	 * 
	 * @return the keypair
	 */
	public KeyPair getFileKeys() {
		return fileKeys;
	}

	/**
	 * Convenience method that returns the public key of the file keys
	 * 
	 * @return the public key
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
	 * @param name the name of the index
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the parent index (used when the parent may be changed)
	 * 
	 * @param parent the parent index
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
	 * Returns the folder that is shared (can be this node or a parent / grand-parent / ... of this node)
	 * 
	 * @return the top folder of the share
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
					if (subfolder.getSharedFlag()) {
						return true;
					}
				}
			}
		}

		// no case above matches
		return false;
	}

	/**
	 * Returns the full path string (starting at the root) of this node
	 * 
	 * @return the full path, whereas names are separated with the operating systems file separator
	 */
	public String getFullPath() {
		if (parent == null) {
			return "";
		} else {
			if (isFile()) {
				return parent.getFullPath() + name;
			} else {
				return parent.getFullPath() + name + FileUtil.getFileSep();
			}
		}
	}

	/**
	 * Converts the index to a file
	 * 
	 * @param root the root folder
	 * @return the file
	 */
	public File asFile(File root) {
		if (parent == null) {
			return root;
		} else {
			return new File(parent.asFile(root), getName());
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
	 * @return the set of users that have access to this folder (read or write)
	 */
	public abstract Set<String> getCalculatedUserList();

	/**
	 * Returns the responsible protection keys (depends of the shared state). The {@link MetaFileSmall} or
	 * {@link MetaFileLarge} and all {@link Chunk}s are protected with this key.
	 * 
	 * @return the protection keys (or the default protection keys, set to the root). The result should never
	 *         be null.
	 */
	public abstract KeyPair getProtectionKeys();

	/**
	 * Convenience method to ask whether the index is a folder
	 * 
	 * @return <code>true</code> if this is an instance of a {@link FolderIndex}. Otherwise, it must be an
	 *         instance of {@link FileIndex}.
	 */
	public abstract boolean isFolder();

	/**
	 * Returns whether the user can write and upload a file / sub-folder to this directory
	 * 
	 * @return <code>true</code> if this user is allowed to write to this folder
	 */
	public abstract boolean canWrite();

	/**
	 * Convenience method to ask whether the index is a file
	 * 
	 * @return <code>true</code> if this is an instance of a {@link FileIndex}. Otherwise, it must be an
	 *         instance of {@link FolderIndex}.
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
		if (obj == null) {
			return false;
		} else if (obj instanceof String) {
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
