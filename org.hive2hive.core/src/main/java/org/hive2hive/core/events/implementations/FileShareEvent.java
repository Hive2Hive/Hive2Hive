package org.hive2hive.core.events.implementations;

import java.io.File;
import java.util.Set;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.model.UserPermission;

public class FileShareEvent extends FileEvent implements IFileShareEvent {

	private final Set<UserPermission> permissions;
	private final String invitedBy;

	public FileShareEvent(File file, Set<UserPermission> permissions, String invitedBy) {
		super(file, false);
		this.permissions = permissions;
		this.invitedBy = invitedBy;
	}

	@Override
	public Set<UserPermission> getUserPermissions() {
		return permissions;
	}

	@Override
	public UserPermission getUserPermission(String userId) {
		for (UserPermission userPermission : permissions) {
			if (userPermission.getUserId().equalsIgnoreCase(userId)) {
				return userPermission;
			}
		}

		return null;
	}

	@Override
	public String getInvitedBy() {
		return invitedBy;
	}
}
