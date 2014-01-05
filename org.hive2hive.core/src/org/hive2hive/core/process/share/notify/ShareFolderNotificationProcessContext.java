package org.hive2hive.core.process.share.notify;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class ShareFolderNotificationProcessContext extends ProcessContext implements IGetMetaContext {

	private final FileTreeNode fileTreeNode;
	private final UserProfileManager profileManager;
	private final FileManager fileManager;

	private MetaDocument metaDocument;

	public ShareFolderNotificationProcessContext(Process process, FileTreeNode fileTreeNode,
			UserProfileManager profileManager, FileManager fileManager) {
		super(process);
		this.fileTreeNode = fileTreeNode;
		this.profileManager = profileManager;
		this.fileManager = fileManager;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public FileTreeNode getFileTreeNode() {
		return fileTreeNode;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

}
