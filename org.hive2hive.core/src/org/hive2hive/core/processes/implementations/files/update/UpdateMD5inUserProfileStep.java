package org.hive2hive.core.processes.implementations.files.update;

import java.io.IOException;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.UpdateFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * A step updating the MD5 hash in the user profile
 * 
 * @author Nico, Seppi
 */
public class UpdateMD5inUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateMD5inUserProfileStep.class);

	private final UpdateFileProcessContext context;
	private final UserProfileManager profileManager;

	private byte[] originalMD5;

	public UpdateMD5inUserProfileStep(UpdateFileProcessContext context, UserProfileManager profileManager) {
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected void doExecute() throws ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();
		byte[] newMD5;
		try {
			newMD5 = EncryptionUtil.generateMD5Hash(context.getFile());
		} catch (IOException e) {
			throw new ProcessExecutionException(
					"The new MD5 hash for the user profile could not be generated.", e);
		}

		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FileIndex index = (FileIndex) userProfile.getFileById(metaFile.getId());
			
			// store hash of meta file
			index.setMetaFileHash(context.consumeHash());
			
			// store for backup
			originalMD5 = index.getMD5();
			if (H2HEncryptionUtil.compareMD5(originalMD5, newMD5)) {
				throw new ProcessExecutionException("Try to create new version with same content.");
			}

			// make and put modifications
			index.setMD5(newMD5);
			logger.debug("Updating the md5 hash in the user profile");
			profileManager.readyToPut(userProfile, getID());

			// store for notification
			context.provideIndex(index);
		} catch (GetFailedException | PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		MetaFile metaFile = (MetaFile) context.consumeMetaFile();
		if (metaFile != null) {
			try {
				// return to original MD5 and put the userProfile
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);
				FileIndex fileNode = (FileIndex) userProfile.getFileById(metaFile.getId());
				fileNode.setMD5(originalMD5);
				profileManager.readyToPut(userProfile, getID());
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
