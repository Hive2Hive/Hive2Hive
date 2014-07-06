package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.context.interfaces.IFile2MetaContext;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Provides the required context to update the meta document
 * 
 * @author Nico, Seppi
 */
public class MetaDocumentPKUpdateContext extends BasePKUpdateContext implements IFile2MetaContext {

	private final PublicKey fileKey;
	private final FileIndex fileIndex;
	private MetaFile metaFile;

	public MetaDocumentPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, PublicKey fileKey,
			FileIndex fileIndex) {
		super(oldProtectionKeys, newProtectionKeys);
		this.fileKey = fileKey;
		this.fileIndex = fileIndex;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		// ignore because this is the old protection key which we have already
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore
	}

	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public String getLocationKey() {
		return H2HDefaultEncryption.key2String(fileKey);
	}

	@Override
	public String getContentKey() {
		return H2HConstants.META_FILE;
	}

	@Override
	public int getTTL() {
		return TimeToLiveStore.getInstance().getMetaFile();
	}

	@Override
	public byte[] getHash() {
		return fileIndex.getMetaFileHash();
	}

	@Override
	public Number160 getVersionKey() {
		return metaFile.getVersionKey();
	}

	public String getFileName() {
		return fileIndex.getName();
	}

	@Override
	public void provideMetaFileEncryptionKeys(KeyPair encryptionKeys) {
		// ignore
	}

	@Override
	public KeyPair consumeMetaFileEncryptionKeys() {
		return fileIndex.getFileKeys();
	}

	@Override
	public File consumeFile() {
		// not used here
		return null;
	}

}