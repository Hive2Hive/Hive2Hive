package org.hive2hive.core.process.share;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

/**
 * A process step which adds a new user permission to the shared meta folder.
 * 
 * @author Seppi
 */
public class UpdateMetaFolderStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateMetaFolderStep.class);

	private boolean modified = false;

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();

		if (context.getMetaDocument() == null) {
			getProcess()
					.stop("Meta folder does not exist, but folder is in user profile. You are in an inconsistent state");
			return;
		}

		logger.debug("Updating meta folder for sharing.");

		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
		if (metaFolder.getUserList().contains(context.getFriendId())) {
			getProcess().stop(
					String.format("The folder is already shared with the user '%s'", context.getFriendId()));
			return;
		}
		metaFolder.addUserPermissions(new UserPermission(context.getFriendId(), PermissionType.WRITE));

		// set modification flag needed for roll backs
		modified = true;

		logger.debug("Putting the modified meta folder (containing the new user permission)");
		PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFolder, new UpdateUserProfileStep());
		getProcess().setNextStep(putMetaStep);
	}

	@Override
	public void rollBack() {
		if (modified) {
			try {
				ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
				MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
				metaFolder.removeUserPermissions(context.getFriendId());
			} catch (Exception e) {
				logger.error(String
						.format("Rollbacking of updating meta folder step (sharing meta folder) failed. exception = '%s'",
								e));
			}
			modified = false;
		}
		getProcess().nextRollBackStep();
	}

}
