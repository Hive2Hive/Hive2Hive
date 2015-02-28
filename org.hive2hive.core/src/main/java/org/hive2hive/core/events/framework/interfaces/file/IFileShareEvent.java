package org.hive2hive.core.events.framework.interfaces.file;

import java.util.Set;

import org.hive2hive.core.model.UserPermission;

public interface IFileShareEvent extends IFileEvent {

	/**
	 * @return the permissions of all users for the shared folder
	 */
	Set<UserPermission> getUserPermissions();

	/**
	 * @param userId the userId to ask the permission. <code>null</code> will be returned in case the userId
	 *            does not have any permission.
	 * @return the permission for this shared folder for the given user
	 */
	UserPermission getUserPermission(String userId);

	/**
	 * @return the host that invited to share
	 */
	String getInvitedBy();
}
