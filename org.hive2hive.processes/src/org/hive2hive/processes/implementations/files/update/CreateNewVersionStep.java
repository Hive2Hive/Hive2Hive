package org.hive2hive.processes.implementations.files.update;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.UpdateFileProcessContext;

/**
 * Creates a new file version.
 * 
 * @author Seppi, Nico
 */
public class CreateNewVersionStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(CreateNewVersionStep.class);

	private final UpdateFileProcessContext context;
	private FileVersion newVersion;

	public CreateNewVersionStep(UpdateFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "Meta document is null."));
			return;
		}

		logger.debug("Adding a new version to the meta file.");

		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();
		newVersion = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(context.getFile()),
				System.currentTimeMillis(), context.getChunkKeys());
		metaFile.getVersions().add(newVersion);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (context.consumeMetaDocument() != null) {
			MetaFile metaFile = (MetaFile) context.consumeMetaDocument();
			metaFile.getVersions().remove(newVersion);
		}
	}
}
