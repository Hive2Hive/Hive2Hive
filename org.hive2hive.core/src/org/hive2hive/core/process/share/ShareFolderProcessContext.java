package org.hive2hive.core.process.share;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetPublicKeyContext;
import org.hive2hive.core.process.context.ProcessContext;

/**
 * Process context for the {@link ShareFolderProcess} process.
 * 
 * @author Seppi
 */
public class ShareFolderProcessContext extends ProcessContext implements IGetMetaContext, IGetPublicKeyContext {

	private final File folderToShare;
	private final String friendId;
	private final H2HSession session;
	
	private PublicKey friendsPublicKey;
	private KeyPair protectionKeys;
	private MetaDocument metaDocument;
	private FileTreeNode fileTreeNode;
	
	public ShareFolderProcessContext(ShareFolderProcess shareFolderProcess, File folderToShare,
			String friendId, H2HSession session) {
		super(shareFolderProcess);
		this.folderToShare = folderToShare;
		this.friendId = friendId;
		this.session = session;
	}

	public File getFolderToShare() {
		return folderToShare;
	}

	public String getFriendId() {
		return friendId;
	}
	
	public H2HSession getSession() {
		return session;
	}

	@Override
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}
	
	public void setFileTreeNode(FileTreeNode fileTreeNode) {
		this.fileTreeNode = fileTreeNode;
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

	@Override
	public void setPublicKey(PublicKey publicKey) {
		this.friendsPublicKey = publicKey;
	}

	@Override
	public PublicKey getPublicKey() {
		return friendsPublicKey;
	}

}
