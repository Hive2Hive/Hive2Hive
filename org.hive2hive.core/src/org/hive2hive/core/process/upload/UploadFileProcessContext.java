package org.hive2hive.core.process.upload;

import java.io.File;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public class UploadFileProcessContext extends ProcessContext {

	private final File file;
	private UserProfile profile;

	public UploadFileProcessContext(UploadFileProcess process, UserProfile profile, File file) {
		super(process);
		this.file = file;
		this.setProfile(profile);
	}

	public File getFile() {
		return file;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}
}
