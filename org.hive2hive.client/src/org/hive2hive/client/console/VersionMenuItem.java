package org.hive2hive.client.console;

import org.hive2hive.core.model.IFileVersion;

public abstract class VersionMenuItem extends H2HConsoleMenuItem {

	protected final IFileVersion fileVersion;

	public VersionMenuItem(IFileVersion fileVersion) {
		super(fileVersion.toString());
		this.fileVersion = fileVersion;
	}
}
