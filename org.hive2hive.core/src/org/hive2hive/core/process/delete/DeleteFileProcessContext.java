package org.hive2hive.core.process.delete;

import java.io.File;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.context.ProcessContext;

public class DeleteFileProcessContext extends ProcessContext implements IGetUserProfileContext,
		IGetMetaContext {

	private final FileManager fileManager;
	private final File file;
	private MetaDocument metaDocument;
	private UserProfile userProfile;

	public DeleteFileProcessContext(Process process, FileManager fileManager, File file) {
		super(process);
		this.fileManager = fileManager;
		this.file = file;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	@Override
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public File getFile() {
		return file;
	}
}
