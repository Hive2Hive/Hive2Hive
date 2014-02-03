package org.hive2hive.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

public class ShareProcessContext implements IProvideProtectionKeys, IConsumeProtectionKeys,
		IProvideMetaDocument, IConsumeMetaDocument, IConsumeNotificationFactory, IProvideNotificationFactory {

	private final File folder;
	private final String friendId;
	private final KeyPair newProtectionKeys;

	private KeyPair oldProtectionKeys;
	private MetaDocument metaDocument;
	private FileTreeNode fileTreeNode;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;

	public ShareProcessContext(File folder, String friendId) {
		this.folder = folder;
		this.friendId = friendId;
		this.newProtectionKeys = EncryptionUtil.generateProtectionKey();
	}

	public File getFolder() {
		return folder;
	}

	public String getFriendId() {
		return friendId;
	}

	public KeyPair consumeNewProtectionKeys() {
		return newProtectionKeys;
	}

	public void setFileTreeNode(FileTreeNode fileTreeNode) {
		this.fileTreeNode = fileTreeNode;
	}

	public FileTreeNode getFileTreeNode() {
		return fileTreeNode;
	}

	public KeyPair consumeOldProtectionKeys() {
		return oldProtectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		// returns the new protection keys to store the meta document, ...
		return newProtectionKeys;
	}

	/**
	 * Note that these are the old protection keys
	 */
	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.oldProtectionKeys = protectionKeys;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return messageFactory;
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return users;
	}

}
