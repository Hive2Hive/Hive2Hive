package org.hive2hive.core.process.upload.newversion;

import java.io.File;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.upload.BaseUploadFileProcessContext;

public class NewVersionProcessContext extends BaseUploadFileProcessContext {

	private GetMetaDocumentStep getMetaStep;

	public NewVersionProcessContext(NewVersionProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process, file, credentials, fileManager, config, true);
	}

	public void setGetMetaDocumentStep(GetMetaDocumentStep getMetaStep) {
		this.getMetaStep = getMetaStep;
	}

	public GetMetaDocumentStep getMetaDocumentStep() {
		return getMetaStep;
	}
}
