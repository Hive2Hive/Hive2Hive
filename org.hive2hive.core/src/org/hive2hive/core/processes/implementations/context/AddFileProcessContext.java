package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public class AddFileProcessContext implements IConsumeProtectionKeys, IConsumeMetaDocument,
		IConsumeNotificationFactory, IProvideNotificationFactory {

	private final File file;
	private final boolean inRoot;

	private List<KeyPair> chunkKeys;
	private KeyPair metaKeyPair;
	private MetaDocument newMetaDocument;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;
	private Index newIndexNode;

	public AddFileProcessContext(File file, boolean inRoot) {
		this.file = file;
		this.inRoot = inRoot;
	}

	public File getFile() {
		return file;
	}

	public boolean isInRoot() {
		return inRoot;
	}

	public void setChunkKeys(List<KeyPair> chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	public List<KeyPair> getChunkKeys() {
		return chunkKeys;
	}

	public void setNewMetaKeyPair(KeyPair metaKeyPair) {
		this.metaKeyPair = metaKeyPair;
	}

	public KeyPair getNewMetaKeyPair() {
		return metaKeyPair;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		// return the new meta document
		return newMetaDocument;
	}

	public void provideNewMetaDocument(MetaDocument newMetaDocument) {
		this.newMetaDocument = newMetaDocument;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return newIndexNode.getProtectionKeys();
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

	public void setNewIndex(Index newIndexNode) {
		this.newIndexNode = newIndexNode;
	}

	public Index getNewIndex() {
		return newIndexNode;
	}
}
