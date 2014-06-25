package org.hive2hive.core.model;

import java.io.Serializable;

/**
 * Mapping between the userId and the permission type. This is applied for {@link MetaFolder} objects in order
 * to identify users rights (and undertake necessary actions in case user leaves or wants to change rights).
 * 
 * @author Nico
 */
public class UserPermission implements Serializable {

	private static final long serialVersionUID = -4564774898662002461L;

	private final String userId;
	private PermissionType permission;

	public UserPermission(String userId, PermissionType permission) {
		this.userId = userId;
		this.setPermission(permission);
	}

	public UserPermission(UserPermission userPermission) {
		this.userId = userPermission.userId;
		this.permission = userPermission.permission;
	}

	public void setPermission(PermissionType permission) {
		this.permission = permission;
	}

	public PermissionType getPermission() {
		return permission;
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserPermission) {
			UserPermission other = (UserPermission) obj;
			return userId.equals(other.getUserId()) && permission == other.permission;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return userId.hashCode() + permission.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("UserPermission [");
		sb.append("userId: ").append(userId);
		sb.append(", permission: ").append(permission.name()).append("]");
		return sb.toString();
	}
}
