package org.hive2hive.processes.implementations.share;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.processes.implementations.context.ShareProcessContext;

/**
 * A process step which adds a new user permission to the shared meta folder.
 * 
 * @author Seppi, Nico
 */
public class UpdateMetaFolderStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateMetaFolderStep.class);
	private final ShareProcessContext context;

	public UpdateMetaFolderStep(ShareProcessContext context, IDataManager dataManager) {
		super(context, context, dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this,
					"Meta folder does not exist, but folder is in user profile. You are in an inconsistent state"));
			return;
		}

		logger.debug("Updating meta folder for sharing.");

		MetaFolder metaFolder = (MetaFolder) context.consumeMetaDocument();
		if (metaFolder.getUserList().contains(context.getFriendId())) {
			cancel(new RollbackReason(this, String.format("The folder is already shared with the user '%s'",
					context.getFriendId())));
			return;
		}
		metaFolder.addUserPermissions(new UserPermission(context.getFriendId(), PermissionType.WRITE));

		logger.debug("Putting the modified meta folder (containing the new user permission)");
		super.doExecute();
	}
}
