package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DeleteFileProcessContext implements IProvideMetaDocument, IConsumeMetaDocument,
		IProvideProtectionKeys, IConsumeProtectionKeys, IProvideNotificationFactory,
		IConsumeNotificationFactory {

	private final boolean isDirectory;
	private final boolean fileInRoot;

	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	private HybridEncryptedContent encryptedMetaDocument;
	private FileTreeNode deletedFileNode;
	private FileTreeNode parentNode;
	private Set<String> users;
	private BaseNotificationMessageFactory messageFactory;

	public DeleteFileProcessContext(boolean isDirectory, boolean fileInRoot) {
		this.isDirectory = isDirectory;
		this.fileInRoot = fileInRoot;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		this.encryptedMetaDocument = encryptedMetaDocument;
	}

	public HybridEncryptedContent getEncryptedMetaDocument() {
		return encryptedMetaDocument;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public boolean isFileInRoot() {
		return fileInRoot;
	}

	public void setDeletedNode(FileTreeNode deletedFileNode) {
		this.deletedFileNode = deletedFileNode;
	}

	public FileTreeNode getDeletedNode() {
		return deletedFileNode;
	}

	public void setParentNode(FileTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	public FileTreeNode getParentNode() {
		return parentNode;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return messageFactory;
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return users;
	}

	@Override
	public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}
}
