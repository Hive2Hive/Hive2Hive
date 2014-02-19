package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeIndex;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideIndex;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public class AddFileProcessContext implements IConsumeProtectionKeys, IConsumeMetaFile,
		IConsumeNotificationFactory, IProvideNotificationFactory, IConsumeIndex, IProvideIndex {

	private final File file;

	private List<KeyPair> chunkKeys;
	private KeyPair metaKeyPair;
	private MetaFile newMetaFile;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;
	private Index index;

	public AddFileProcessContext(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
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
	public MetaFile consumeMetaFile() {
		// return the new meta file
		return newMetaFile;
	}

	public void provideNewMetaFile(MetaFile newMetaFile) {
		this.newMetaFile = newMetaFile;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return index.getProtectionKeys();
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

	@Override
	public void provideIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}
}
