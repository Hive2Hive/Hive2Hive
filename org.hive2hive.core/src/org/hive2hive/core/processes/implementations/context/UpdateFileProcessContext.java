package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UpdateFileProcessContext extends AddFileProcessContext implements IProvideMetaFile,
		IProvideProtectionKeys {

	// protection keys of the existing meta file
	private KeyPair protectionKeys;

	// the chunk keys to delete (if the configuration does not allow as many or as big chunks as existent)
	private List<KeyPair> chunksToDelete;
	private KeyPair chunksToDeleteProtectionKeys;
	private KeyPair parentFileKey;

	public UpdateFileProcessContext(File file) {
		super(file);
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		provideNewMetaFile(metaFile);
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	public List<KeyPair> getChunksToDelete() {
		return chunksToDelete;
	}

	public void setChunksToDelete(List<KeyPair> chunksToDelete) {
		this.chunksToDelete = chunksToDelete;
	}

	public void setChunksToDeleteProtectionKeys(KeyPair protectionKeys) {
		this.chunksToDeleteProtectionKeys = protectionKeys;
	}

	public KeyPair getChunksToDeleteProtectionKeys() {
		return chunksToDeleteProtectionKeys;
	}

	public void setParentFileKey(KeyPair parentFileKey) {
		this.parentFileKey = parentFileKey;
	}

	public KeyPair getParentFileKey() {
		return parentFileKey;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

}
