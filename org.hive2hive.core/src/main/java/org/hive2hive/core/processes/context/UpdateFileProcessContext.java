package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.processes.context.interfaces.IGetFileKeysContext;
import org.hive2hive.core.processes.context.interfaces.IGetMetaFileContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.context.interfaces.IUploadContext;
import org.hive2hive.core.processes.files.update.UpdateNotificationMessageFactory;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class UpdateFileProcessContext implements IUploadContext, IGetFileKeysContext, IGetMetaFileContext, INotifyContext {

	private final File file;
	private final H2HSession session;

	private List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();

	private KeyPair chunkProtectionKeys;
	private KeyPair metaFileProtectionKeys;
	private KeyPair metaFileEncryptionKeys;
	private boolean largeFile;
	private BaseMetaFile metaFile;
	private byte[] hash;
	private FileIndex index;
	private Set<String> users;
	private UpdateNotificationMessageFactory messageFactory;
	private List<MetaChunk> chunksToDelete;

	public UpdateFileProcessContext(File file, H2HSession session) {
		this.file = file;
		this.session = session;
	}

	@Override
	public File consumeFile() {
		return file;
	}

	@Override
	public File consumeRoot() {
		return session.getRootFile();
	}

	@Override
	public void provideMetaFileProtectionKeys(KeyPair metaFileProtectionKeys) {
		this.metaFileProtectionKeys = metaFileProtectionKeys;
	}

	@Override
	public KeyPair consumeMetaFileProtectionKeys() {
		return metaFileProtectionKeys;
	}

	@Override
	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys) {
		this.metaFileEncryptionKeys = encryptionKeys;
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return metaFileEncryptionKeys;
	}

	@Override
	public void provideChunkProtectionKeys(KeyPair chunkProtectionKeys) {
		this.chunkProtectionKeys = chunkProtectionKeys;
	}

	@Override
	public KeyPair consumeChunkProtectionKeys() {
		return chunkProtectionKeys;
	}

	@Override
	public void provideChunkEncryptionKeys(KeyPair chunkKeys) {
		// not used here
	}

	@Override
	public KeyPair consumeChunkEncryptionKeys() {
		if (metaFile instanceof MetaFileSmall) {
			return ((MetaFileSmall) metaFile).getChunkKey();
		}
		return null;
	}

	public List<MetaChunk> getChunksToDelete() {
		return chunksToDelete;
	}

	public void setChunksToDelete(List<MetaChunk> chunksToDelete) {
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public boolean allowLargeFile() {
		return false;
	}

	@Override
	public IFileConfiguration consumeFileConfiguration() {
		return session.getFileConfiguration();
	}

	@Override
	public void setLargeFile(boolean largeFile) {
		this.largeFile = largeFile;
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
	public boolean isLargeFile() {
		return largeFile;
	}

	@Override
	public List<MetaChunk> getMetaChunks() {
		return metaChunks;
	}

	@Override
	public BaseMetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public void provideMetaFileHash(byte[] hash) {
		this.hash = hash;
	}

	public byte[] consumeHash() {
		return hash;
	}

	public void provideIndex(FileIndex index) {
		this.index = index;
	}

	@Override
	public FileIndex consumeIndex() {
		return index;
	}

	@Override
	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	public void provideMessageFactory(UpdateNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
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
