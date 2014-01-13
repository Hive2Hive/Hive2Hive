package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Create a new {@link MetaDocument}.
 * 
 * @author Seppi
 */
public class CreateMetaDocumentStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CreateMetaDocumentStep.class);

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		File file = context.getFile();

		// generate the new key pair for the meta file (which are later stored in the user profile)
		KeyPair metaKeyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT_RSA);
		context.setNewMetaKeyPair(metaKeyPair);

		MetaDocument metaDocument = null;
		if (file.isDirectory()) {
			// create a new meta folder
			metaDocument = new MetaFolder(metaKeyPair.getPublic(), file.getName(), context.getH2HSession()
					.getCredentials().getUserId());
			logger.debug(String.format("New meta folder created. folder = '%s'", file.getName()));
		} else {
			// create new meta file with new version
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis());
			context.setChunkKeys(version.getChunkIds());
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);

			metaDocument = new MetaFile(metaKeyPair.getPublic(), file.getName(), versions);
			logger.debug(String.format("New meta file created. file = '%s'", file.getName()));			
		}
		context.setNewMetaDocument(metaDocument);
		
		getProcess().setNextStep(new GetParentMetaStep());
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}

}
