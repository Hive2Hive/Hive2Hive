package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

public class NewFileProcessContext extends UploadFileProcessContext {

	private KeyPair keyPair;

	public NewFileProcessContext(NewFileProcess process, File file, UserProfileManager profileManager,
			FileManager fileManager, IFileConfiguration config) {
		super(process, file, profileManager, fileManager, config, false);
	}

	public KeyPair getNewMetaKeyPair() {
		return keyPair;
	}

	public void setNewMetaKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}
}
