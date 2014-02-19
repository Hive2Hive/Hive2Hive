package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DeleteFileProcessContext implements IProvideMetaFile, IConsumeMetaFile, IProvideProtectionKeys,
		IConsumeProtectionKeys, IProvideNotificationFactory, IConsumeNotificationFactory {

	private final boolean isDirectory;

	private MetaFile metaFile;
	private KeyPair protectionKeys;
	private HybridEncryptedContent encryptedMetaFile;
	private Index deletedIndex;
	private Index parentNode;
	private Set<String> users;
	private BaseNotificationMessageFactory messageFactory;

	public DeleteFileProcessContext(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		this.encryptedMetaFile = encryptedMetaDocument;
	}

	public HybridEncryptedContent consumeEncryptedMetaFile() {
		return encryptedMetaFile;
	}

	@Override
	public MetaFile consumeMetaFile() {
		return metaFile;
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
