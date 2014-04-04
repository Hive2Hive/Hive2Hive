package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;

/**
 * Create a new {@link MetaFile}.
 * 
 * @author Nico, Chris, Seppi
 */
public class CreateMetaFileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CreateMetaFileStep.class);
	
	private final AddFileProcessContext context;

	public CreateMetaFileStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		File file = context.getFile();

		logger.trace(String.format("Creating new meta file for file '%s'", file.getName()));

		// create new meta file with new version
		FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis(),
				context.getMetaChunks());
		List<FileVersion> versions = new ArrayList<FileVersion>(1);
		versions.add(version);
		MetaFile metaFile = new MetaFile(context.getMetaKeys().getPublic(), versions, context.consumeChunkKeys());

		context.provideMetaFile(metaFile);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// remove provided meta file
		context.provideMetaFile(null);
	}
}
