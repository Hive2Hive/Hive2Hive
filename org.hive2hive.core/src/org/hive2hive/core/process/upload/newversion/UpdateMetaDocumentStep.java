package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateMetaDocumentStep extends ProcessStep {

	@Override
	public void start() {
		NewVersionProcessContext context = (NewVersionProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocumentStep().getMetaDocument();
		if (metaDocument == null) {
			getProcess()
					.stop("Meta document does not exist, but file is in user profile. You are in an inconsistent state");
			return;
		}

		File file = context.getFile();
		if (file.isDirectory()) {
			// no need to put because meta folder remains the same
			// what to do when updating directory?
			// TODO: inform other clients
			getProcess().setNextStep(getNextStep());
		} else {
			MetaFile metaFile = (MetaFile) metaDocument;
			List<KeyPair> chunkKeys = context.getChunkKeys();
			FileVersion version = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(file),
					System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			metaFile.getVersions().add(version);

			PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFile, getNextStep());
			getProcess().setNextStep(putMetaStep);
		}
	}

	public ProcessStep getNextStep() {
		// TODO inform other clients
		return null;
	}

	@Override
	public void rollBack() {
		// nothing to do
	}
}
