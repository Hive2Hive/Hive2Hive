package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.upload.PutChunkStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;

public class PutNewFileChunkStep extends PutChunkStep {

	public PutNewFileChunkStep(File file, NewFileProcessContext context) {
		super(file, 0, new ArrayList<KeyPair>());
		configureStepAfterUpload(context);
	}

	private void configureStepAfterUpload(NewFileProcessContext context) {
		// generate the new key pair for the meta file (which are later stored in the user profile)
		KeyPair metaKeyPair = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		MetaDocument metaDocument = null;
		if (file.isDirectory()) {
			// create a new meta folder
			metaDocument = new MetaFolder(metaKeyPair.getPublic(), context.getCredentials().getUserId());
		} else {
			// create new meta file with new version
			MetaFile metaFile = new MetaFile(metaKeyPair.getPublic());
			FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			List<FileVersion> versions = new ArrayList<FileVersion>(1);
			versions.add(version);
			metaFile.setVersions(versions);
			metaDocument = metaFile;
		}

		// 1. put the meta document
		// 2. get the user profile
		// 3. update the user profile
		UpdateUserProfileStep updateProfileStep = new UpdateUserProfileStep(file, metaKeyPair,
				context.getCredentials());
		GetUserProfileStep getProfileStep = new GetUserProfileStep(context.getCredentials(),
				updateProfileStep, context);
		PutMetaDocumentStep putMetaFolder = new PutMetaDocumentStep(metaDocument, getProfileStep);

		setStepAfterPutting(putMetaFolder);
	}
}
