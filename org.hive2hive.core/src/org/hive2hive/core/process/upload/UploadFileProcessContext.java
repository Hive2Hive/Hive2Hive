package org.hive2hive.core.process.upload;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.context.ProcessContext;

public class UploadFileProcessContext extends ProcessContext implements IGetUserProfileContext,
		IGetMetaContext {

	private final FileManager fileManager;
	private final IH2HFileConfiguration config;
	private final UserCredentials credentials;
	private final File file;
	private final boolean fileAlreadyExists;
	private List<KeyPair> chunkKeys;
	private UserProfile userProfile;
	private MetaDocument metaDocument;

	public UploadFileProcessContext(Process process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config, boolean fileAlreadyExists) {
		super(process);
		this.file = file;
		this.credentials = credentials;
		this.fileManager = fileManager;
		this.config = config;
		this.fileAlreadyExists = fileAlreadyExists;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public IH2HFileConfiguration getConfig() {
		return config;
	}

	public UserCredentials getCredentials() {
		return credentials;
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
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
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
