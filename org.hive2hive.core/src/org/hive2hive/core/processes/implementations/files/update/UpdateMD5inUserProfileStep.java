package org.hive2hive.core.processes.implementations.files.update;

import java.io.IOException;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.UpdateFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * A step updating the MD5 hash in the user profile
 * 
 * @author Nico
 * 
 */
public class UpdateMD5inUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateMD5inUserProfileStep.class);

	private final UpdateFileProcessContext context;

	private byte[] originalMD5;

	public UpdateMD5inUserProfileStep(UpdateFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();
		byte[] newMD5;
		try {
			newMD5 = EncryptionUtil.generateMD5Hash(context.getFile());
		} catch (IOException e) {
			cancel(new RollbackReason(this, "The new MD5 hash for the user profile could not be generated"));
			return;
		}

		try {
			UserProfileManager profileManager = context.getH2HSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FileTreeNode fileNode = userProfile.getFileById(metaFile.getId());

			// store for backup
			originalMD5 = fileNode.getMD5();
			if (H2HEncryptionUtil.compareMD5(originalMD5, newMD5)) {
				cancel(new RollbackReason(this, "Try to create new version with same content."));
				return;
			}

			// make and put modifications
			fileNode.setMD5(newMD5);
			logger.debug("Updating the md5 hash in the user profile");
			profileManager.readyToPut(userProfile, getID());
		} catch (GetFailedException | PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();
		if (metaFile != null) {
			// return to original MD5 and put the userProfile
			UserProfileManager profileManager = context.getH2HSession().getProfileManager();
			try {
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);
				FileTreeNode fileNode = userProfile.getFileById(metaFile.getId());
				fileNode.setMD5(originalMD5);
				profileManager.readyToPut(userProfile, getID());
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
