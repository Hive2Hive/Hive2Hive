package org.hive2hive.core.processes.context.interfaces;

import java.io.File;
import java.security.KeyPair;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.BaseMetaFile;

public interface IUploadContext {

	public File consumeFile();

	// ------ CheckWriteAccessStep, AddIndexToUserProfileStep ------

	public File consumeRoot();

	// ------ PrepareNotifictionStep ------

	public Index consumeIndex();

	public void provideUsersToNotify(Set<String> users);

	// ------ PutMetaFileStep, CreateNewVersionStep, UpdateMD5InUserProfileStep ------

	public BaseMetaFile consumeMetaFile();

	// ------ PutMetaFileStep ------

	public KeyPair consumeMetaFileProtectionKeys();

	public KeyPair consumeMetaFileEncryptionKeys();

	public void provideMetaFileHash(byte[] hash);

	// ------ PutSingleChunkStep, CleanupChunksStep ------

	public KeyPair consumeChunkProtectionKeys();

	// ------ CheckWriteAccessStep ------

	public void provideChunkProtectionKeys(KeyPair chunkProtectionKeys);

	public void provideMetaFileProtectionKeys(KeyPair metaFileProtectionKeys);

	// ------ ValidateFileSizeStep ------

	public boolean allowLargeFile();

	public void setLargeFile(boolean largeFile);

	// ------ InitializeChunksStep ------

	public void provideChunkEncryptionKeys(KeyPair chunkEncryptionKeys);

	// ------ InitializeChunksStep, CreateMetaFileStep ------

	public boolean isLargeFile();

	// ------ CreateMetaFileStep, PutSingleChunkStep, InitializeChunksStep ------

	public KeyPair consumeChunkEncryptionKeys();

	// ------ PutSingleChunkStep, CreateMetaFileStep, CreateNewVersionStep, InitializeChunksStep ------

	public List<MetaChunk> getMetaChunks();

	// ------ ValidateFileSizeStep, InitializeChunksStep, PutSingleChunkStep, CreateNewVersionStep ------

	public IFileConfiguration consumeFileConfiguration();

}
