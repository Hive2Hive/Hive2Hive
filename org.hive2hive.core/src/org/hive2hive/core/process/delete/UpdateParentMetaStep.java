package org.hive2hive.core.process.delete;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;

/**
 * Updates the parent meta data such that the child is removed from the list
 * 
 * @author Nico
 * 
 */
public class UpdateParentMetaStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateParentMetaStep.class);
	private final KeyPair childKey;

	public UpdateParentMetaStep(KeyPair childKey) {
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

		parentMeta.removeChildDocument(childKey);
		logger.debug("Removed child from meta folder. Total children = "
				+ parentMeta.getChildDocuments().size());

		// Hint: The user profile has already been updated before

		// TODO notify other clients
		super.nextStep = new PutUserProfileStep(context.getUserProfile(), context.getCredentials(), null);
		super.metaDocument = parentMeta;
		super.start();
	}
}
