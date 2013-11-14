package org.hive2hive.core.process.upload.newfile;

import java.io.File;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.process.upload.BaseUploadFileProcessContext;
import org.hive2hive.core.security.UserCredentials;

public class NewFileProcessContext extends BaseUploadFileProcessContext {

	public NewFileProcessContext(NewFileProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process, file, credentials, fileManager, config, false);
	}
}
