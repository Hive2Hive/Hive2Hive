package org.hive2hive.core.process.move;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateSourceParentStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateSourceParentStep.class);

	public UpdateSourceParentStep() {
		super(null, null);
	}

	@Override
	public void start() {
		logger.debug("Start removing the file from the former parent meta folder");
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		MetaDocument sourceParent = context.getMetaDocument();
		if (sourceParent == null) {
			getProcess().stop("Parent meta folder of source not found");
			return;
		}

		MetaFolder parent = (MetaFolder) sourceParent;
		PublicKey fileKey = context.getFileNodeKeys().getPublic();
		parent.removeChildKey(fileKey);
		super.metaDocument = parent;

		// keep the list of users to notify them about the movement
		context.addUsersToNotifySource(parent.getUserList());

		if (context.getDestinationParentKeys() == null) {
			logger.debug("No need to update the new parent meta folder since it's moved to root");
			// file is going to be in root. Next steps:
			// 1. update the user profile
			// 2. notify
			super.nextStep = new RelinkUserProfileStep();
		} else {
			// initialize next steps:
			// 1. get parent of destination
			// 2. add the new child
			// 3. update the user profile
			// 4. notify
			super.nextStep = new GetMetaDocumentStep(context.getDestinationParentKeys(),
					new UpdateDestinationParentStep(), context);
		}

		super.start();
	}
}
