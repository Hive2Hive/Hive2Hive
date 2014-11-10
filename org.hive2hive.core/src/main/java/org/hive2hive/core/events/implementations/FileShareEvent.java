package org.hive2hive.core.events.implementations;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.model.UserPermission;

public class FileShareEvent extends FileEvent implements IFileAddEvent {

	private final UserPermission permission;
	private final String invitedBy;

	public FileShareEvent(File file, boolean isFile, UserPermission permission, String invitedBy) {
		super(file, isFile);
		this.permission = permission;
		this.invitedBy = invitedBy;
	}

	/**
	 * @return the permission for this shared folder
	 */
	public UserPermission getPermission() {
		return permission;
	}

	/**
	 * @return the host that invited to share
	 */
	public String getInvitedBy() {
		return invitedBy;
	}
}
