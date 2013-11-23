package org.hive2hive.core.process.delete;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

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

		parentMeta.removeChildDocument(childKey);
		logger.debug("Removed child from meta folder. Total children = "
				+ parentMeta.getChildDocuments().size());

		// TODO notify other clients
		// nextStep = ...
		super.metaDocument = parentMeta;
		super.start();
	}
}
