package org.hive2hive.core.processes.implementations.share;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
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
	private final DataManager dataManager;
	private String locationKey;
	private String contentKey;

	public UpdateMetaFolderStep(ShareProcessContext context, DataManager dataManager) {
		super(context, context, dataManager);
		this.context = context;
		this.dataManager = dataManager;
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
		metaFolder.addUserPermissions(new UserPermission(context.getFriendId(), context.getPermission()));

		logger.debug("Putting the modified meta folder (containing the new user permission '"
				+ context.getPermission().name() + "')");
		super.doExecute();
	}

	@Override
	protected void put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey)
			throws PutFailedException {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		dataManager.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(contentKey), content, context.consumeOldProtectionKeys(),
				context.consumeNewProtectionKeys());
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (putPerformed) {
			boolean success = dataManager.remove(locationKey, contentKey, context.consumeMetaDocument()
					.getVersionKey(), context.consumeNewProtectionKeys());
			if (success) {
				logger.debug("Successfully removed the meta folder version during rollback");
			} else {
				logger.error("Could not remove the meta folder version during rollback");
			}
		}
	}
}
