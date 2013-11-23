package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;

import org.hive2hive.core.process.common.File2MetaFileStep;
import org.hive2hive.core.process.upload.PutChunkStep;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

public class PutNewVersionChunkStep extends PutChunkStep {

	/**
	 * Constructor for first call
	 * 
	 * @param file
	 */
	public PutNewVersionChunkStep(File file, UploadFileProcessContext context) {
		super(file, 0, new ArrayList<KeyPair>());
		configureStepAfterUpload(context);
	}

	private void configureStepAfterUpload(UploadFileProcessContext context) {
		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, context.getProfileManager(),
				context.getFileManager(), context, new UpdateMetaDocumentStep());
		setStepAfterPutting(file2MetaStep);
	}
}
