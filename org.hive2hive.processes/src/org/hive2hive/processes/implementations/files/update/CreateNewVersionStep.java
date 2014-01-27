package org.hive2hive.processes.implementations.files.update;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.IFileConfiguration;
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

	// used for rollback
	private FileVersion newVersion;
	private final List<FileVersion> deletedFileVersions;

	public CreateNewVersionStep(UpdateFileProcessContext context) {
		this.context = context;
		this.deletedFileVersions = new ArrayList<FileVersion>();
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

		initiateCleanup();
	}

	private void initiateCleanup() {
		IFileConfiguration fileConfiguration = context.getH2HSession().getFileConfiguration();
		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();

		// remove files when the number of allowed versions is exceeded or when the total file size (sum
		// of all versions) exceeds the allowed file size
		while (metaFile.getVersions().size() > fileConfiguration.getMaxNumOfVersions()
				|| metaFile.getTotalSize() > fileConfiguration.getMaxSizeAllVersions()) {
			// keep at least one version
			if (metaFile.getVersions().size() == 1)
				break;

			// remove the version of the meta file
			deletedFileVersions.add(metaFile.getVersions().remove(0));
		}

		logger.debug(String.format("Need to remove %s old versions", deletedFileVersions.size()));
		List<KeyPair> chunksToDelete = new ArrayList<KeyPair>();
		for (FileVersion fileVersion : deletedFileVersions) {
			chunksToDelete.addAll(fileVersion.getChunkKeys());
		}

		context.setChunksToDelete(chunksToDelete);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (context.consumeMetaDocument() != null) {
			MetaFile metaFile = (MetaFile) context.consumeMetaDocument();
			// remove the new version
			metaFile.getVersions().remove(newVersion);

			// add the cleaned up versions
			metaFile.getVersions().addAll(deletedFileVersions);
		}
	}
}
