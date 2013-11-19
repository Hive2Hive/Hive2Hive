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
	private final List<KeyPair> childDocuments; // TODO really used??

	public MetaFolder(PublicKey id, String creatorName) {
		super(id);
		userPermissions = new ArrayList<UserPermission>();
		childDocuments = new ArrayList<KeyPair>();

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

	public List<KeyPair> getChildDocuments() {
		return childDocuments;
	}

	public void addChildDocument(KeyPair child) {
		childDocuments.add(child);
	}

	public void removeChildDocument(KeyPair child) {
		childDocuments.remove(child);
	}
}
