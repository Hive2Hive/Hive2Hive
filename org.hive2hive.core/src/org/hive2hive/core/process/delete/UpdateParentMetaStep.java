package org.hive2hive.core.process.delete;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.notify.NotifyPeersProcess;

/**
 * Updates the parent meta data such that the child is removed from the list
 * 
 * @author Nico
 * 
 */
public class UpdateParentMetaStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateParentMetaStep.class);
	private final PublicKey childKey;

	public UpdateParentMetaStep(PublicKey childKey) {
		super(null);
		this.childKey = childKey;
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();

		// remove the child from the parent meta data
		MetaFolder parentMeta = (MetaFolder) context.getMetaDocument();
		if (parentMeta == null) {
			getProcess().stop("Could not find the parent meta data");
		}

		parentMeta.removeChildKey(childKey);
		logger.debug("Removed child from meta folder. Total children = "
				+ parentMeta.getChildKeys().size());

		// notify other clients (can be multiple users)
		NotifyPeersProcess notifyProcess = new NotifyPeersProcess(getNetworkManager(),
				parentMeta.getUserList(), new DeleteNotifyMessageFactory(childKey));
		notifyProcess.start();

		// next step is null, process is done
		super.metaDocument = parentMeta;
		super.start();
	}
}
