package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UpdateFileProcessContext extends AddFileProcessContext implements IProvideMetaDocument {

	// the chunk keys to delete (if the configuration does not allow as many or as big chunks as existent)
	private List<KeyPair> chunksToDelete;
	private KeyPair protectionsKeys;
	private KeyPair parentFileKey;

	public UpdateFileProcessContext(File file, boolean inRoot, H2HSession session) {
		super(file, inRoot, session);
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		provideNewMetaDocument(metaDocument);
	}

	@Override
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	public List<KeyPair> getChunksToDelete() {
		return chunksToDelete;
	}

	public void setChunksToDelete(List<KeyPair> chunksToDelete) {
		this.chunksToDelete = chunksToDelete;
	}

	public void setChunksToDeleteProtectionKeys(KeyPair protectionsKeys) {
		this.protectionsKeys = protectionsKeys;
	}

	public KeyPair getChunksToDeleteProtectionKeys() {
		return protectionsKeys;
	}

	public void setParentFileKey(KeyPair parentFileKey) {
		this.parentFileKey = parentFileKey;
	}

	public KeyPair getParentFileKey() {
		return parentFileKey;
	}
}
