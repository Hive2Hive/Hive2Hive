package org.hive2hive.core.events.framework.interfaces.file;

import org.hive2hive.core.model.UserPermission;

public interface IFileShareEvent extends IFileEvent {

	/**
	 * @return the permission for this shared folder
	 */
	public UserPermission getUserPermission();

	/**
	 * @return the host that invited to share
	 */
	String getInvitedBy();
}
