package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;

/**
 * Create a new {@link MetaDocument}.
 * 
 * @author Nico, Chris
 */
public class CreateMetaDocumentStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CreateMetaDocumentStep.class);
	private final AddFileProcessContext context;
	private final String userId;

	public CreateMetaDocumentStep(AddFileProcessContext context, String userId) {
		this.context = context;
		this.userId = userId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		File file = context.getFile();
		KeyPair metaKeyPair = context.getNewMetaKeyPair();

		MetaDocument metaDocument = null;
		if (file.isDirectory()) {
			// create a new meta folder
			metaDocument = new MetaFolder(metaKeyPair.getPublic(), file.getName(), userId);
			logger.debug(String.format("New meta folder created. folder = '%s'", file.getName()));
		} else {
			// create new meta file with new version
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis(),
					context.getChunkKeys());
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);

			metaDocument = new MetaFile(metaKeyPair.getPublic(), file.getName(), versions);
			logger.debug(String.format("New meta file created. file = '%s'", file.getName()));
		}

		context.provideNewMetaDocument(metaDocument);
	}
}
