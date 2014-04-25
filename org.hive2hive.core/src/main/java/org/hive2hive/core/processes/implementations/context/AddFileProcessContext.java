package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideHash;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * The context for the process of putting a file.
 * 
 * @author Nico, Seppi
 */
public class AddFileProcessContext implements IProvideHash, IConsumeNotificationFactory, IProvideProtectionKeys,
		IProvideMetaFile {

	private final File file;

	private KeyPair metaKeys;
	private List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();

	private KeyPair chunkEncryptionKeys;
	private MetaFile metaFile;
	private byte[] hash;
	private KeyPair protectionKeys;

	private Index index;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;

	private boolean largeFile;

	public AddFileProcessContext(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void setLargeFile(boolean largeFile) {
		this.largeFile = largeFile;
	}

	public boolean isLargeFile() {
		return largeFile;
	}

	public KeyPair getMetaKeys() {
		if (metaKeys == null)
			metaKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		return metaKeys;
	}

	public List<MetaChunk> getMetaChunks() {
		return metaChunks;
	}

	public void provideChunkKeys(KeyPair chunkEncryptionKeys) {
		this.chunkEncryptionKeys = chunkEncryptionKeys;
	}

	public KeyPair consumeChunkKeys() {
		return chunkEncryptionKeys;
	}

	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideHash(byte[] hash) {
		this.hash = hash;
	}

	public byte[] consumeHash() {
		return hash;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	public Index consumeIndex() {
		return index;
	}

	public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return messageFactory;
	}

	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	public Set<String> consumeUsersToNotify() {
		return users;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaFile) {
		// never used in this context
	}
}
