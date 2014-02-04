package org.hive2hive.core.processes.implementations.files.move;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext;

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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Start adding the file to the new parent meta folder.");
		MetaDocument destinationParent = context.consumeMetaDocument();
		if (destinationParent == null) {
			throw new ProcessExecutionException("Parent meta folder of destination not found.");
		}

		KeyPair parentProtectionKeys = context.consumeProtectionKeys();
		if (parentProtectionKeys == null) {
			throw new ProcessExecutionException("Parent meta folder of destination content protection keys not found.");
		}

		MetaFolder parent = (MetaFolder) destinationParent;
		parent.addChildKeyPair(context.getFileNodeKeys());

		// keep the list of users to notify them about the movement
		context.addUsersToNotifyDestination(parent.getUserList());

		super.doExecute();
	}
}
