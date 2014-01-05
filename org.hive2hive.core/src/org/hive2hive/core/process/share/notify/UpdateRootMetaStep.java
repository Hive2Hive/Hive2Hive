package org.hive2hive.core.process.share.notify;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

/**
 * Adds the key pair of the newly shared folder as a child to the meta folder of the root. All newly added
 * shared folder will be located in the root folder of the logged in user.
 * 
 * @author Seppi
 */
public class UpdateRootMetaStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateRootMetaStep.class);

	public UpdateRootMetaStep() {
		super(null, new UpdateUserProfileStep());
	}

	@Override
	public void start() {
		ShareFolderNotificationProcessContext context = (ShareFolderNotificationProcessContext) getProcess()
				.getContext();
		logger.debug("Start updating the root meta folder which is parent of the shared folder.");

		MetaFolder rootMeta = (MetaFolder) context.getMetaDocument();
		if (rootMeta == null) {
			getProcess().stop("Could not find the root meta data");
			return;
		}

		// add child (shared folder) to the root meta data
		rootMeta.addChildKeyPair(context.getFileTreeNode().getKeyPair());
		logger.debug("MetaFolder of the root has new child (shared folder).");

		super.metaDocument = rootMeta;
		super.start();
	}
}
