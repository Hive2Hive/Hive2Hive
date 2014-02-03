package org.hive2hive.processes.implementations.files.move;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.processes.implementations.context.MoveFileProcessContext;

/**
 * A process step which updates the destination parent meta folder of a move process.
 * 
 * @author Nico, Seppi
 */
public class UpdateDestinationParentStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateDestinationParentStep.class);
	private final MoveFileProcessContext context;

	public UpdateDestinationParentStep(MoveFileProcessContext context, IDataManager dataManager) {
		super(context, context, dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Start adding the file to the new parent meta folder.");
		MetaDocument destinationParent = context.consumeMetaDocument();
		if (destinationParent == null) {
			cancel(new RollbackReason(this, "Parent meta folder of destination not found."));
			return;
		}

		KeyPair parentProtectionKeys = context.consumeProtectionKeys();
		if (parentProtectionKeys == null) {
			cancel(new RollbackReason(this,
					"Parent meta folder of destination content protection keys not found."));
			return;
		}

		MetaFolder parent = (MetaFolder) destinationParent;
		parent.addChildKeyPair(context.getFileNodeKeys());

		// keep the list of users to notify them about the movement
		context.addUsersToNotifyDestination(parent.getUserList());

		super.doExecute();
	}
}
