package org.hive2hive.core.process.upload.newfile;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateParentMetaStep extends PutMetaDocumentStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateParentMetaStep.class);

	protected UpdateParentMetaStep() {
		super(new UpdateUserProfileStep());
	}

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();

		// add child to the parent meta data
		MetaFolder parentMeta = (MetaFolder) context.getMetaDocument();
		if (parentMeta == null) {
			getProcess().stop("Could not find the parent meta data");
		}

		parentMeta.addChildDocument(context.getNewMetaKeyPair());
		logger.debug("MetaFolder has new child. Total children = " + parentMeta.getChildDocuments().size());

		super.metaDocument = parentMeta;
		super.start();
	}
}
