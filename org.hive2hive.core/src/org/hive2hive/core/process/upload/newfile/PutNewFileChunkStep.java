package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.upload.PutChunkStep;
import org.hive2hive.core.security.EncryptionUtil;

public class PutNewFileChunkStep extends PutChunkStep {

	public PutNewFileChunkStep(File file, NewFileProcessContext context) {
		super(file, 0, new ArrayList<KeyPair>());
		configureStepAfterUpload(context);
	}

	private void configureStepAfterUpload(NewFileProcessContext context) {
		// generate the new key pair for the meta file (which are later stored in the user profile)
		KeyPair metaKeyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);
		context.setNewMetaKeyPair(metaKeyPair);

		MetaDocument metaDocument = null;
		if (file.isDirectory()) {
			// create a new meta folder
			metaDocument = new MetaFolder(metaKeyPair.getPublic(), file.getName(), context.getCredentials().getUserId());
		} else {
			// create new meta file with new version
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);

			MetaFile metaFile = new MetaFile(metaKeyPair.getPublic(), file.getName(), versions);
			metaDocument = metaFile;
		}

		// 1. get the parent meta document
		// 2. put the new meta document
		// 3. update the parent meta document
		// 4. update the user profile
		setStepAfterPutting(new GetParentMetaStep(metaDocument));
	}
}
