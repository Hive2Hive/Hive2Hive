package org.hive2hive.core.process.delete;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class DeleteFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final FileManager fileManager;
	private final boolean isDirectory;
	private final UserProfileManager profileManager;
	private MetaDocument metaDocument;

	public DeleteFileProcessContext(Process process, FileManager fileManager, boolean isDirectory,
			UserProfileManager profileManager) {
		super(process);
		this.fileManager = fileManager;
		this.isDirectory = isDirectory;
		this.profileManager = profileManager;
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

	public boolean isDirectory() {
		return isDirectory;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}
}
