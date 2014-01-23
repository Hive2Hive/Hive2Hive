package org.hive2hive.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

public class AddFileProcessContext implements IConsumeProtectionKeys, IProvideProtectionKeys,
		IConsumeMetaDocument, IConsumeNotificationFactory, IProvideNotificationFactory {

	private final File file;
	private final H2HSession session;
	private final boolean inRoot;

	private List<KeyPair> chunkKeys;
	private KeyPair metaKeyPair;
	private MetaDocument parentMetaDocument;
	private KeyPair protectionKeys;
	private MetaDocument newMetaDocument;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;

	public AddFileProcessContext(File file, boolean inRoot, H2HSession session) {
		this.file = file;
		this.inRoot = inRoot;
		this.session = session;
	}

	public File getFile() {
		return file;
	}

	public H2HSession getH2HSession() {
		return session;
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

	public MetaDocument consumeParentMetaDocument() {
		return parentMetaDocument;
	}

	public void provideParentMetaDocument(MetaDocument metaDocument) {
		this.parentMetaDocument = metaDocument;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public boolean isInRoot() {
		return inRoot;
	}

	@Override
	public void setMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public void setUsers(Set<String> users) {
		this.users = users;
	}

	@Override
	public BaseNotificationMessageFactory provideMessageFactory() {
		return messageFactory;
	}

	@Override
	public Set<String> getUsers() {
		return users;
	}
}
