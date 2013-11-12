package org.hive2hive.core.process.upload;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateMetaDocumentStep extends PutMetaDocumentStep {

	public UpdateMetaDocumentStep() {
		super(null, null);
	}

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		metaDocument = context.getMetaDocumentStep().getMetaDocument();
		if (metaDocument == null) {
			getProcess().stop("Meta document does not exist, but file is in user profile");
			return;
		}

		File file = context.getFile();
		if (!file.isDirectory()) {
			MetaFile metaFile = (MetaFile) metaDocument;
			List<KeyPair> chunkKeys = context.getChunkKeys();
			FileVersion version = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(file),
					System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			metaFile.getVersions().add(version);
			super.start();
		} else {
			// no need to put because meta folder remains the same
			// what to do when updating directory?
			// TODO: inform other clients
			getProcess().setNextStep(null);
		}
	}
}
