package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.files.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.IH2HEncryption;

public class DeleteFileProcessContext implements IGetMetaFileContext, INotifyContext {

	private final File file;
	private final H2HSession session;
	private final IH2HEncryption encrpytion;

	private KeyPair protectionKeys;
	private KeyPair encryptionKeys;
	private BaseMetaFile metaFile;
	private Index index;
	private DeleteNotifyMessageFactory deleteNotifyMessageFactory;
	private Set<String> users;

	public DeleteFileProcessContext(File file, H2HSession session, IH2HEncryption encrpytion) {
		this.file = file;
		this.session = session;
		this.encrpytion = encrpytion;
	}

	public File consumeFile() {
		return file;
	}

	public File consumeRoot() {
		return session.getRootFile();
	}

	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys) {
		this.encryptionKeys = encryptionKeys;
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return encryptionKeys;
	}

	@Override
	public void provideMetaFile(BaseMetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaFile) {
		// not used here
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return deleteNotifyMessageFactory;
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return users;
	}

	public void provideMessageFactory(DeleteNotifyMessageFactory deleteNotifyMessageFactory) {
		this.deleteNotifyMessageFactory = deleteNotifyMessageFactory;
	}

	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	public Index consumeIndex() {
		return index;
	}

	public BaseMetaFile consumeMetaFile() {
		return metaFile;
	}

	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public KeyPair consumeEncryptedMetaFile() {
		return encryptionKeys;
	}

	public IH2HEncryption getEncryption() {
		return encrpytion;
	}

}