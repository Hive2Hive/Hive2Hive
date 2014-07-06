package org.hive2hive.core.processes.files.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileLarge;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new {@link MetaFileSmall}.
 * 
 * @author Nico, Chris, Seppi
 */
public class CreateMetaFileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(CreateMetaFileStep.class);
	private final AddFileProcessContext context;

	public CreateMetaFileStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		File file = context.consumeFile();

		logger.trace("Creating new meta file for file '{}'.", file.getName());

		MetaFile metaFile = null;
		if (context.isLargeFile()) {
			metaFile = new MetaFileLarge(context.generateOrGetMetaKeys().getPublic(), context.getMetaChunks());
		} else {
			// create new meta file with new version
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis(),
					context.getMetaChunks());
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);
			metaFile = new MetaFileSmall(context.generateOrGetMetaKeys().getPublic(), versions,
					context.consumeChunkKeys());
		}
		context.provideMetaFile(metaFile);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// remove provided meta file
		context.provideMetaFile(null);
	}
}
