package org.hive2hive.core.processes.context;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.processes.context.interfaces.ICheckWriteAccessContext;
import org.hive2hive.core.processes.context.interfaces.IFile2MetaContext;
import org.hive2hive.core.processes.context.interfaces.IInitializeChunksStepContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.context.interfaces.IPrepareNotificationContext;
import org.hive2hive.core.processes.context.interfaces.IPutMetaFileContext;
import org.hive2hive.core.processes.context.interfaces.IValidateFileSizeContext;
import org.hive2hive.core.processes.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UpdateFileProcessContext implements IValidateFileSizeContext, ICheckWriteAccessContext, IFile2MetaContext,
		IInitializeChunksStepContext, IPutMetaFileContext, IPrepareNotificationContext, INotifyContext {

	private final File file;
	private final H2HSession session;

	private List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();
	// the chunk keys to delete (if the configuration does not allow as many or as big chunks as existent)
	private List<MetaChunk> chunksToDelete;

	private KeyPair protectionKeys;
	private KeyPair encryptionKeys;
	private boolean largeFile;
	private MetaFile metaFile;
	private byte[] hash;
	private Index index;
	private Set<String> users;
	private UploadNotificationMessageFactory messageFactory;

	public UpdateFileProcessContext(File file, H2HSession session) {
		this.file = file;
		this.session = session;
	}

	@Override
	public File consumeFile() {
		return file;
	}

	@Override
	public Path consumeRoot() {
		return session.getRoot();
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	public List<MetaChunk> getChunksToDelete() {
		return chunksToDelete;
	}

	public void setChunksToDelete(List<MetaChunk> chunksToDelete) {
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public KeyPair consumeChunkKeys() {
		if (metaFile instanceof MetaFileSmall) {
			return ((MetaFileSmall) metaFile).getChunkKey();
		}
		return null;
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
	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys) {
		this.encryptionKeys = encryptionKeys;
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return encryptionKeys;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
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
	public void provideChunkKeys(KeyPair chunkKeys) {
		// not used here
	}

	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public KeyPair consumeMetaFileProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void provideMetaFileHash(byte[] hash) {
		this.hash = hash;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public byte[] consumeHash() {
		return hash;
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}

	@Override
	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	@Override
	public void provideMessageFactory(UploadNotificationMessageFactory messageFactory) {
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
