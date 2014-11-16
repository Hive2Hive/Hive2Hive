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
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Create a new {@link MetaFileSmall} or {@link MetaFileLarge}.
 * 
 * @author Nico, Chris, Seppi
 */
public class CreateMetaFileStep extends ProcessStep<Void> {

	private final AddFileProcessContext context;

	public CreateMetaFileStep(AddFileProcessContext context) {
		this.context = context;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException {
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
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		context.provideMetaFile(null);
		setRequiresRollback(false);
		return null;
	}
}
