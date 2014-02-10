package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DeleteFileProcessContext implements IProvideMetaDocument, IConsumeMetaDocument,
		IProvideProtectionKeys, IConsumeProtectionKeys, IProvideNotificationFactory,
		IConsumeNotificationFactory {

	private final boolean isDirectory;

	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	private HybridEncryptedContent encryptedMetaDocument;
	private Index deletedIndex;
	private Index parentNode;
	private Set<String> users;
	private BaseNotificationMessageFactory messageFactory;

	public DeleteFileProcessContext(boolean isDirectory) {
		this.isDirectory = isDirectory;
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

	public void setDeletedIndex(Index deletedIndex) {
		this.deletedIndex = deletedIndex;
	}

	public Index getDeletedIndex() {
		return deletedIndex;
	}

	public void setParentNode(Index parentNode) {
		this.parentNode = parentNode;
	}

	public Index getParentNode() {
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
