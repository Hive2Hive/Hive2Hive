package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.security.EncryptionUtil;

public class UpdateMetaDocumentStep extends ProcessStep {

	@Override
	public void start() {
		NewVersionProcessContext context = (NewVersionProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			getProcess()
					.stop("Meta document does not exist, but file is in user profile. You are in an inconsistent state");
			return;
		}

		File file = context.getFile();
		if (file.isDirectory()) {
			// no need to put because meta folder remains the same
			getProcess().setNextStep(getInformClientsStep());
		} else {
			MetaFile metaFile = (MetaFile) metaDocument;
			List<KeyPair> chunkKeys = context.getChunkKeys();
			FileVersion version = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(file),
					System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			metaFile.getVersions().add(version);

			// 1. update the md5 hash in the user profile
			// 2. put the meta document
			// 3. put the user profile
			// 4. inform other clients
			UserProfile userProfile = context.getUserProfile();
			FileTreeNode fileNode = userProfile.getFileById(metaFile.getId());
			try {
				byte[] newMD5 = EncryptionUtil.generateMD5Hash(file);
				fileNode.setMD5(newMD5);
				PutUserProfileStep putUserProfile = new PutUserProfileStep(userProfile,
						context.getCredentials(), getInformClientsStep());

				PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFile, putUserProfile);
				getProcess().setNextStep(putMetaStep);
			} catch (IOException e) {
				getProcess().stop("The MD5 hash in the user profile could not be generated");
			}
		}
	}

	public ProcessStep getInformClientsStep() {
		// TODO inform other clients
		return null;
	}

	@Override
	public void rollBack() {
		// nothing to do
	}
}
