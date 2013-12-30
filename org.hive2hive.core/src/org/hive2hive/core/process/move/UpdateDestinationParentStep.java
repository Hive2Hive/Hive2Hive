package org.hive2hive.core.process.move;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateDestinationParentStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateDestinationParentStep.class);

	public UpdateDestinationParentStep() {
		super(null, new RelinkUserProfileStep());
	}

	@Override
	public void start() {
		logger.debug("Start adding the file to the new parent meta folder");
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		MetaDocument destinationParent = context.getMetaDocument();
		if (destinationParent == null) {
			getProcess().stop("Parent meta folder of destination not found");
			return;
		}

		MetaFolder parent = (MetaFolder) destinationParent;
		parent.addChildKeyPair(context.getFileNodeKeys());
		super.metaDocument = parent;

		// keep the list of users to notify them about the movement
		context.addUsersToNotifyDestination(parent.getUserList());

		super.start();
	}
}
