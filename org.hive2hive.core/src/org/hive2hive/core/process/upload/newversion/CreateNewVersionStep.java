package org.hive2hive.core.process.upload.newversion;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.upload.PutChunkStep;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

/**
 * Creates a new file version.
 * 
 * @author Seppi
 */
public class CreateNewVersionStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(CreateNewVersionStep.class);

	private UploadFileProcessContext context;
	private MetaFile metaFile;
	private FileVersion newVersion;

	@Override
	public void start() {
		context = (UploadFileProcessContext) getProcess().getContext();

		if (context.getMetaDocument() == null) {
			getProcess().stop("Meta document is null.");
			return;
		}

		logger.debug("Adding a new version to the meta file.");

		metaFile = (MetaFile) context.getMetaDocument();
		newVersion = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(context.getFile()),
				System.currentTimeMillis());
		metaFile.getVersions().add(newVersion);
		context.setChunkKeys(newVersion.getChunkIds());

		PutChunkStep putChunkStep = new PutChunkStep(new UpdateMetaFileStep());
		getProcess().setNextStep(putChunkStep);
	}

	@Override
	public void rollBack() {
		if (metaFile != null) {
			metaFile.getVersions().remove(newVersion);
			context.setChunkKeys(null);
		}
		getProcess().nextRollBackStep();
	}

}
