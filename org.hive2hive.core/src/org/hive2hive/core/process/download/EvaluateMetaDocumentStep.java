package org.hive2hive.core.process.download;

import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

public class EvaluateMetaDocumentStep extends ProcessStep {

	@Override
	public void start() {
		DownloadFileProcessContext context = (DownloadFileProcessContext) getProcess().getContext();
		GetMetaDocumentStep metaDocumentStep = context.getMetaDocumentStep();
		if (metaDocumentStep.getMetaDocument() == null) {
			// no meta document found
			getProcess().stop("Meta document not found");
		} else {
			MetaFile metaFile = (MetaFile) metaDocumentStep.getMetaDocument();
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
