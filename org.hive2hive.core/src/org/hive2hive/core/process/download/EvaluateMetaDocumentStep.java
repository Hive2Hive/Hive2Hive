package org.hive2hive.core.process.download;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.ProcessStep;

public class EvaluateMetaDocumentStep extends ProcessStep {

	@Override
	public void start() {
		DownloadFileProcessContext context = (DownloadFileProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			// no meta document found
			getProcess().stop("Meta document not found");
		} else {
			MetaFile metaFile = (MetaFile) metaDocument;
			GetFileChunkStep nextStep = new GetFileChunkStep(context.getFile(), metaFile,
					context.getFileManager());
			getProcess().setNextStep(nextStep);
		}
	}

	@Override
	public void rollBack() {
		// nothing to do
	}

}
