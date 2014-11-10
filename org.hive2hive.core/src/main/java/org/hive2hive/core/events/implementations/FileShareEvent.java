package org.hive2hive.core.events.implementations;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.model.UserPermission;

public class FileShareEvent extends FileEvent implements IFileShareEvent {

	private final UserPermission permission;
	private final String invitedBy;

	public FileShareEvent(File file, UserPermission permission, String invitedBy) {
		super(file, false);
		this.permission = permission;
		this.invitedBy = invitedBy;
	}

	@Override
	public UserPermission getUserPermission() {
		return permission;
	}

	@Override
	public String getInvitedBy() {
		return invitedBy;
	}
}
