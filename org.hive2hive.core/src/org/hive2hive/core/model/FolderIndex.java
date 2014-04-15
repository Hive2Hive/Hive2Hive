package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hive2hive.core.file.FileUtil;

/**
 * An index stored in the user profile that represents a folder in the directory. It has some additional
 * capabilities than files like sharing.
 * 
 * @author Nico
 * 
 */
public class FolderIndex extends Index {

	private static final long serialVersionUID = 3798065400562165454L;

	private final Set<Index> children = new HashSet<Index>();
	private final Set<UserPermission> userPermissions = new HashSet<UserPermission>();

	private KeyPair protectionKeys = null;
	private boolean isShared = false;

	/**
	 * Constructor for child nodes of type 'folder'
	 * 
	 * @param parent
	 * @param keyPair
	 * @param name
	 * @param isFolder
	 */
	// TODO keypair can be generated here, no need to hand over as parameter
	public FolderIndex(FolderIndex parent, KeyPair keyPair, String name) {
		super(keyPair, name, parent);
	}

	/**
	 * Returns whether this index is the root
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Returns all direct children of this node
	 * 
	 * @return all childrens of this node
	 */
	public Set<Index> getChildren() {
		return children;
	}

	/**
	 * Add a child to the index. The child can either represent a file or a folder
	 * 
	 * @param child
	 */
	public void addChild(Index child) {
		// only add once
		if (getChildByName(child.getName()) == null)
			children.add(child);
	}

	/**
	 * Remove a child from the index tree
	 * 
	 * @param child
	 */
	public void removeChild(Index child) {
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
	// TODO get child by full path??
	public Index getChildByName(String name) {
		if (name != null) {
			String withoutSeparator = name.replaceAll(FileUtil.getFileSep(), "");
			for (Index child : children) {
				if (child.getName().equalsIgnoreCase(withoutSeparator)) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Indicate that this node is shared
	 * 
	 * @param protectionKeys if the user has write access, the protection keys are != null, else, they can be
	 *            null.
	 */
	public void share(KeyPair protectionKeys) throws IllegalStateException {
		if (isRoot())
			throw new IllegalStateException("Root node can't be shared.");
		else if (isSharedOrHasSharedChildren()) {
			throw new IllegalStateException("This folder or any child is already shared");
		}

		this.isShared = true;
		this.protectionKeys = protectionKeys;
	}

	/**
	 * Takes all actions to indicate that this folder is private again.
	 */
	public void unshare() {
		this.isShared = false;
		this.protectionKeys = null;

		userPermissions.clear();
	}

	/**
	 * Add a permission for a user to read / write that directory (all sub-directories inherit this
	 * permission)
	 * 
	 * @param userPermission
	 */
	public void addUserPermissions(UserPermission userPermission) {
		userPermissions.add(userPermission);
	}

	/**
	 * Remove a user's permission. Note that you never should remove your own permission of your root!
	 * 
	 * @param userId
	 */
	public void removeUserPermissions(String userId) {
		Iterator<UserPermission> iter = userPermissions.iterator();
		while (iter.hasNext()) {
			UserPermission userPermission = iter.next();
			if (userPermission.getUserId().equalsIgnoreCase(userId)) {
				iter.remove();
			}
		}
	}

	/**
	 * Returns a list of the permissions for this node. Note that the returned permissions are not inherited
	 * from nodes above; Only the assigned permissions for this node are returned.
	 * 
	 * @return the user permissions of this index.
	 */
	public Set<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	/**
	 * Returns a list of the permissions of this node together with the inherited permissions from the parent
	 * nodes.
	 * 
	 * @return
	 */
	public Set<UserPermission> getCalculatedUserPermissions() {
		// if there is a parent and no user permissions, ask parent
		if (parent != null && userPermissions.isEmpty())
			return parent.getCalculatedUserPermissions();

		// if there is no parent or user permission list is not empty
		return userPermissions;
	}

	@Override
	public Set<String> getCalculatedUserList() {
		// gather the own user list
		Set<String> users = new HashSet<String>();
		for (UserPermission permission : userPermissions) {
			users.add(permission.getUserId());
		}

		// combine with parent user list (recursion)
		if (parent != null)
			users.addAll(parent.getCalculatedUserList());

		return users;
	}

	@Override
	public boolean canWrite() {
		if (isShared()) {
			// shared (sub) folder, write depends whether the parent has protection keys
			return getProtectionKeys() != null;
		} else {
			// can always write to a private folder
			return true;
		}
	}

	/**
	 * Returns whether a specific user can write to this folder.
	 * 
	 * @param userId
	 * @return
	 */
	public boolean canWrite(String userId) {
		for (UserPermission permission : getCalculatedUserPermissions()) {
			if (permission.getUserId().equals(userId)) {
				return permission.getPermission() == PermissionType.WRITE;
			}
		}

		return false;
	}

	/**
	 * Set the protection keys. This method should only be used when creating a root node or when changing the
	 * protection keys at shared folders!
	 * 
	 * @param protectionKeys the protection keys that are responsible for all children or sub-folders (if they
	 *            don't have own protection keys).
	 */
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair getProtectionKeys() {
		if (isShared) {
			// the shared flag is on, return the protection keys (can be null)
			return protectionKeys;
		}

		if (protectionKeys == null) {
			// inherit parent's protection keys
			return parent.getProtectionKeys();
		}

		// is root
		return protectionKeys;
	}

	@Override
	public boolean isShared() {
		if (isShared) {
			// this folder is shared
			return true;
		}

		if (isRoot()) {
			// root is never shared
			return false;
		}

		// ask the parent whether this subfolder is shared
		return parent.isShared();
	}

	/**
	 * Returns the flag whether this node is shared (this is only set at the top shared folder, not at all
	 * sub-children). This call should be used with care.
	 * 
	 * @return
	 */
	public boolean getSharedFlag() {
		return isShared;
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FolderIndex [");
		sb.append("name=").append(name);
		sb.append(" path=").append(getFullPath());
		sb.append(" children=").append(children.size()).append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Index) {
			// compare the name and the keys
			Index other = (Index) obj;
			if (getFilePublicKey().equals(other.getFilePublicKey())) {
				// keys match, does the name match too?
				if (getName() == null) {
					// this is root, other maybe too
					return other.getName() == null;
				}

				return getName().equals(other.getName());
			}
		} else if (obj instanceof PublicKey) {
			// compare the keys only
			PublicKey publicKey = (PublicKey) obj;
			return getFilePublicKey().equals(publicKey);
		}

		return false;
	}
}
