package org.hive2hive.core.process.share;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class ShareFolderProcessContext extends ProcessContext implements IGetMetaContext {

	private final File folderToShare;
	private final String friendId;
	private final FileManager fileManager;
	private final UserProfileManager profileManager;
	private KeyPair domainKey;
	private MetaDocument metaDocument;

	public ShareFolderProcessContext(Process process, File folderToShare, String friendId,
			UserProfileManager userProfileManager, FileManager fileManager) {
		super(process);
		this.folderToShare = folderToShare;
		this.friendId = friendId;
		this.fileManager = fileManager;
		this.profileManager = userProfileManager;
	}

	public File getFolderToShare() {
		return folderToShare;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public String getFriendId() {
		return friendId;
	}

	public void setDomainKey(KeyPair domainKey) {
		this.domainKey = domainKey;
	}

	public KeyPair getDomainKey() {
		return domainKey;
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
