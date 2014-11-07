package org.hive2hive.core.processes.files.add;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.MetaFileLarge;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Create a new {@link MetaFileSmall} or {@link MetaFileLarge}.
 * 
 * @author Nico, Chris, Seppi
 */
public class CreateMetaFileStep extends ProcessStep {

	private final AddFileProcessContext context;

	public CreateMetaFileStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		File file = context.consumeFile();
		KeyPair metaKeys = context.consumeMetaFileEncryptionKeys();

		BaseMetaFile metaFile = null;
		if (context.isLargeFile()) {
			metaFile = new MetaFileLarge(metaKeys.getPublic(), context.getMetaChunks());
		} else {
			// create new meta file with new version
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis(),
					context.getMetaChunks());
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);
			metaFile = new MetaFileSmall(metaKeys.getPublic(), versions, context.consumeChunkEncryptionKeys());
		}
		context.provideMetaFile(metaFile);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		context.provideMetaFile(null);
	}
}
