package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the permissions for the folder (only one if private, but can have multiple users) and the keys for
 * the containing documents
 * 
 * @author Nico
 * 
 */
public class MetaFolder extends MetaDocument {

	private static final long serialVersionUID = 1L;
	private final List<UserPermission> userPermissions;
	private final List<KeyPair> childKeys;

	public MetaFolder(PublicKey id, String creatorName) {
		super(id);
		userPermissions = new ArrayList<UserPermission>();
		childKeys = new ArrayList<KeyPair>();

		// creator receives write permissions by default
		userPermissions.add(new UserPermission(creatorName, PermissionType.WRITE));
	}

	public List<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	public void addUserPermissions(UserPermission userPermission) {
		userPermissions.add(userPermission);
	}

	public void removeUserPermissions(String userId) {
		UserPermission toDelete = null;
		for (UserPermission permission : userPermissions) {
			if (permission.getUserId().equalsIgnoreCase(userId)) {
				toDelete = permission;
				break;
			}
		}

		userPermissions.remove(toDelete);
	}

	public List<KeyPair> getChildKeys() {
		return childKeys;
	}

	public void addChildKeyPair(KeyPair keyPair) {
		childKeys.add(keyPair);
	}

	public void removeChildKey(PublicKey childKey) {
		KeyPair toRemove = null;
		for (KeyPair child : childKeys) {
			if (child.getPublic().equals(childKey)) {
				toRemove = child;
				break;
			}
		}

		childKeys.remove(toRemove);
	}
}
