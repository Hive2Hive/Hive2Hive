package org.hive2hive.core.model;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public MetaFolder(PublicKey id, String folderName, String creatorName) {
		super(id, folderName);
		userPermissions = new ArrayList<UserPermission>();

		// creator receives write permissions by default
		userPermissions.add(new UserPermission(creatorName, PermissionType.WRITE));
	}

	public List<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	public void addUserPermissions(UserPermission userPermission) {
		if (userPermission != null)
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

	/**
	 * Returns a list of users that can at least read the file
	 * 
	 * @return
	 */
	public Set<String> getUserList() {
		Set<String> users = new HashSet<String>(userPermissions.size());
		for (UserPermission permission : userPermissions) {
			users.add(permission.getUserId());
		}
		return users;
	}
}
