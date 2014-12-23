package org.hive2hive.core.processes.files.list;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Gives information about a file in the DHT. Links to child nodes and parent node (if existing)
 * 
 * @author Nico
 * 
 */
public class FileNode {

	// can be null in case of the root
	private final FileNode parent;
	// holds all children
	private final List<FileNode> children;
	private final String path;
	private final File file;
	private final byte[] md5;
	private final Set<UserPermission> userPermissions;

	FileNode(FileNode parent, File file, String path, byte[] md5, Set<UserPermission> userPermissions) {
		this.parent = parent;
		this.file = file;
		this.path = path;
		this.md5 = md5;
		this.userPermissions = userPermissions;
		this.children = new ArrayList<FileNode>();
	}

	/**
	 * Get the parent node or <code>null</code> if this node is the root folder
	 * 
	 * @return the parent of this node (can be null in case of the root folder)
	 */
	public FileNode getParent() {
		return parent;
	}

	/**
	 * Get child nodes. Note that this may be empty
	 * 
	 * @return the children (can be an empty set) or <code>null</code> if this node is a file and not a folder
	 */
	public List<FileNode> getChildren() {
		if (isFile()) {
			return null;
		}
		return children;
	}

	/**
	 * The name of the file
	 * 
	 * @return the filename
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Returns the relative path of the file to the root
	 * 
	 * @return the full path of the file
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the absolute file with respect to the currently logged in user
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns whether the file is a file and not a directory. This is just the reverse option of
	 * {@link FileNode#isFolder()}.
	 * 
	 * @return true when the file is a file and not a folder.
	 */
	public boolean isFile() {
		if (file.exists()) {
			return file.isFile();
		} else {
			return md5 != null;
		}
	}

	/**
	 * Returns whether the file is a directory. This is just the reverse option of {@link FileNode#isFile()}.
	 * 
	 * @return true when the file is a folder.
	 */
	public boolean isFolder() {
		return !isFile();
	}

	/**
	 * The MD5 hash of the file. In case of a folder, this is null.
	 * 
	 * @return the MD5 hash of the newest file version
	 */
	public byte[] getMd5() {
		return md5;
	}

	/**
	 * A list of users that have permissions to this file (including the user itself)
	 * 
	 * @return the users having (any) permission to this file
	 */
	public Set<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	/**
	 * Indicates whether this file is shared with someone
	 * 
	 * @return true when the file is shared
	 */
	public boolean isShared() {
		return getUserPermissions().size() > 1;
	}

	@Override
	public String toString() {
		return String.format("%s: %s [%s] %s", isFile() ? "File" : "Folder", getPath(), getUserPermissions(),
				isFile() ? String.format("(MD5: %s)", EncryptionUtil.byteToHex(getMd5())) : "");
	}

	/**
	 * Returns a list of all nodes in preorder. One can specify whether only files, folders or both is desired
	 */
	public static List<FileNode> getNodeList(FileNode root, boolean addFiles, boolean addFolders) {
		if (!addFiles && !addFolders) {
			throw new IllegalArgumentException("Must visit either files, folders or both");
		}
		List<FileNode> list = new ArrayList<FileNode>();
		preorder(root, list, addFiles, addFolders);
		return list;
	}

	private static void preorder(FileNode current, List<FileNode> list, boolean addFiles, boolean addFolders) {
		if (current == null) {
			return;
		}

		if (current.isFile() && addFiles || current.isFolder() && addFolders) {
			list.add(current);
		}

		if (current.isFolder()) {
			for (FileNode child : current.getChildren()) {
				preorder(child, list, addFiles, addFolders);
			}
		}
	}
}
