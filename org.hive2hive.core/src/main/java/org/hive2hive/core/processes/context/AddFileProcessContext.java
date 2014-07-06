package org.hive2hive.core.processes.context;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.context.interfaces.ICheckWriteAccessContext;
import org.hive2hive.core.processes.context.interfaces.IInitializeChunksStepContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.context.interfaces.IPrepareNotificationContext;
import org.hive2hive.core.processes.context.interfaces.IPutMetaFileContext;
import org.hive2hive.core.processes.context.interfaces.IValidateFileSizeContext;
import org.hive2hive.core.processes.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * The context for the process of putting a file.
 * 
 * @author Nico, Seppi
 */
public class AddFileProcessContext implements IValidateFileSizeContext, ICheckWriteAccessContext,
		IInitializeChunksStepContext, IPutMetaFileContext, IPrepareNotificationContext, INotifyContext {

	private final File file;
	private final H2HSession session;

	private List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();

	private boolean largeFile;
	private KeyPair chunkKeys;
	private KeyPair protectionKeys;
	private MetaFile metaFile;
	private KeyPair metaKeys;
	private Index index;
	private Set<String> usersToNotify;
	private UploadNotificationMessageFactory messageFactory;

	public AddFileProcessContext(File file, H2HSession session) {
		this.file = file;
		this.session = session;
	}

	@Override
	public File consumeFile() {
		return file;
	}

	@Override
	public void setLargeFile(boolean largeFile) {
		this.largeFile = largeFile;
	}

	@Override
	public IFileConfiguration consumeFileConfiguration() {
		return session.getFileConfiguration();
	}

	@Override
	public Path consumeRoot() {
		return session.getRoot();
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public boolean isLargeFile() {
		return largeFile;
	}

	@Override
	public KeyPair consumeChunkKeys() {
		return chunkKeys;
	}

	@Override
	public void provideChunkKeys(KeyPair chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	@Override
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
		if (metaKeys == null) {
			metaKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		}
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

	@Override
	public Index consumeIndex() {
		return index;
	}

	@Override
	public void provideUsersToNotify(Set<String> users) {
		this.usersToNotify = users;
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
		return usersToNotify;
	}

	@Override
	public boolean allowLargeFile() {
		return true;
	}

}
