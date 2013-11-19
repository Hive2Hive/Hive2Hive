package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

public class NewFileProcessContext extends UploadFileProcessContext {

	private KeyPair keyPair;

	public NewFileProcessContext(NewFileProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process, file, credentials, fileManager, config, false);
	}

	public KeyPair getNewMetaKeyPair() {
		return keyPair;
	}

	public void setNewMetaKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}
}
