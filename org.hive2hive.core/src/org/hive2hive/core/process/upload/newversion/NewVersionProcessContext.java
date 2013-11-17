package org.hive2hive.core.process.upload.newversion;

import java.io.File;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.upload.BaseUploadFileProcessContext;

public class NewVersionProcessContext extends BaseUploadFileProcessContext implements IGetMetaContext {

	private MetaDocument metaDocument;

	public NewVersionProcessContext(NewVersionProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process, file, credentials, fileManager, config, true);
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
