package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.common.INotifyContext;
import org.hive2hive.core.processes.implementations.context.interfaces.common.IPutMetaFileContext;
import org.hive2hive.core.processes.implementations.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * The context for the process of putting a file.
 * 
 * @author Nico, Seppi
 */
public class AddFileProcessContext implements IPutMetaFileContext, INotifyContext {

	private final File file;
	private final H2HSession session;

	private List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();

	private boolean largeFile;
	private KeyPair chunkKeys;
	private KeyPair protectionKeys;
	private MetaFile metaFile;
	private KeyPair metaKeys;
	private Index index;
	private HashSet<String> usersToNotify;
	private UploadNotificationMessageFactory messageFactory;

	public AddFileProcessContext(File file, H2HSession session) {
		this.file = file;
		this.session = session;
	}

	public File consumeFile() {
		return file;
	}

	public void setLargeFile(boolean largeFile) {
		this.largeFile = largeFile;
	}

	public IFileConfiguration consumeFileConfiguration() {
		return session.getFileConfiguration();
	}

	public Path consumeRoot() {
		return session.getRoot();
	}

	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	public boolean isLargeFile() {
		return largeFile;
	}

	public KeyPair consumeChunkKeys() {
		return chunkKeys;
	}

	public void provideChunkKeys(KeyPair chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	public List<MetaChunk> getMetaChunks() {
		return metaChunks;
	}

	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public KeyPair generateOrGetMetaKeys() {
		if (metaKeys == null)
			metaKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		return metaKeys;
	}

	@Override
	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public KeyPair consumeMetaFileProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void provideMetaFileHash(byte[] hash) {
		// not used so far
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	public Index consumeIndex() {
		return index;
	}

	public void provideUsersToNotify(HashSet<String> users) {
		this.usersToNotify = users;
	}

	public void provideMessageFactory(UploadNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return messageFactory;
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return usersToNotify;
	}

}
