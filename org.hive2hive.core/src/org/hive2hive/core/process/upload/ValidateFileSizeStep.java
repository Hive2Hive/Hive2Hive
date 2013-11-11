package org.hive2hive.core.process.upload;

import java.io.File;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.process.ProcessStep;

public class ValidateFileSizeStep extends ProcessStep {

	private final File file;

	public ValidateFileSizeStep(File file) {
		this.file = file;
	}

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		IH2HFileConfiguration config = context.getConfig();

		long fileSize = FileUtil.getFileSize(file);

		if (fileSize > config.getMaxFileSize()) {
			getProcess().stop("File is too large");
		} else {
			// start chunking the file
			PutFileChunkStep chunkingStep = new PutFileChunkStep(file);
			getProcess().setNextStep(chunkingStep);
		}
	}

	@Override
	public void rollBack() {
		// nothing to do
	}

}
