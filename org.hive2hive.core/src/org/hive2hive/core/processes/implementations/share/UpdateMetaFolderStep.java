package org.hive2hive.core.processes.implementations.share;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;

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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.consumeMetaDocument() == null) {
			throw new ProcessExecutionException("Meta folder does not exist, but folder is in user profile. You are in an inconsistent state.");
		}

		logger.debug("Updating meta folder for sharing.");

		MetaFolder metaFolder = (MetaFolder) context.consumeMetaDocument();
		if (metaFolder.getUserList().contains(context.getFriendId())) {
			throw new ProcessExecutionException(String.format("The folder is already shared with the user '%s'",
					context.getFriendId()));
		}
		metaFolder.addUserPermissions(new UserPermission(context.getFriendId(), PermissionType.WRITE));

		logger.debug("Putting the modified meta folder (containing the new user permission)");
		super.doExecute();
	}
}
