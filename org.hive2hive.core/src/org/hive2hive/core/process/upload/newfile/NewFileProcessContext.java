package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.upload.BaseUploadFileProcessContext;

public class NewFileProcessContext extends BaseUploadFileProcessContext implements IGetMetaContext {

	private MetaDocument metaDocument;
	private KeyPair keyPair;

	public NewFileProcessContext(NewFileProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process, file, credentials, fileManager, config, false);
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	public KeyPair getNewMetaKeyPair() {
		return keyPair;
	}

	public void setNewMetaKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}
}
