package org.hive2hive.core.model;

/**
 * Mapping between the userId and the permission type. This is applied for {@link MetaFolder} objects in order
 * to identify users rights (and undertake necessary actions in case user leaves or wants to change rights)
 * 
 * @author Nico
 * 
 */
public class UserPermission {

	private PermissionType permission;
	private final String userId;

	public UserPermission(String userId, PermissionType permission) {
		this.userId = userId;
		this.setPermission(permission);
	}

	public PermissionType getPermission() {
		return permission;
	}

	public void setPermission(PermissionType permission) {
		this.permission = permission;
	}

	public String getUserId() {
		return userId;
	}
}
