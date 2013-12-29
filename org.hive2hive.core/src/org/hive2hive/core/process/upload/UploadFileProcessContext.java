package org.hive2hive.core.process.upload;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.UserCredentials;

public class UploadFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final FileManager fileManager;
	private final IFileConfiguration config;
	private final File file;
	private final boolean fileAlreadyExists;
	private final UserProfileManager profileManager;
	private List<KeyPair> chunkKeys;
	private MetaDocument metaDocument;

	public UploadFileProcessContext(Process process, File file, UserProfileManager profileManager,
			FileManager fileManager, IFileConfiguration config, boolean fileAlreadyExists) {
		super(process);
		this.file = file;
		this.profileManager = profileManager;
		this.fileManager = fileManager;
		this.config = config;
		this.fileAlreadyExists = fileAlreadyExists;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public IFileConfiguration getConfig() {
		return config;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public UserCredentials getCredentials() {
		return profileManager.getUserCredentials();
	}

	public File getFile() {
		return file;
	}

	public boolean getFileAlreadyExists() {
		return fileAlreadyExists;
	}

	public void setChunkKeys(List<KeyPair> chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	public List<KeyPair> getChunkKeys() {
		return chunkKeys;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}
}
