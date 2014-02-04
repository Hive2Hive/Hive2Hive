package org.hive2hive.core.processes.implementations.files.move;

import java.security.KeyPair;
import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext;

public class UpdateSourceParentStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateSourceParentStep.class);
	private final MoveFileProcessContext context;

	public UpdateSourceParentStep(MoveFileProcessContext context, IDataManager dataManager) {
		super(context, context, dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Start removing the file from the former parent meta folder.");

		MetaDocument sourceParent = context.consumeMetaDocument();
		if (sourceParent == null) {
			throw new ProcessExecutionException("Parent meta folder (source) not found.");
		}

		KeyPair sourceParentProtectionKeys = context.consumeProtectionKeys();
		if (sourceParentProtectionKeys == null) {
			throw new ProcessExecutionException("Parent meta folder's (source) content protection keys not found.");
		}

		MetaFolder parent = (MetaFolder) sourceParent;
		PublicKey fileKey = context.getFileNodeKeys().getPublic();
		parent.removeChildKey(fileKey);

		// keep the list of users to notify them about the movement
		context.addUsersToNotifySource(parent.getUserList());

		super.doExecute();
	}
}
