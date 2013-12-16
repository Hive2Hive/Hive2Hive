package org.hive2hive.core.process.move;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;

public class UpdateDestinationParentStep extends PutMetaDocumentStep {

	public UpdateDestinationParentStep() {
		super(null, null);
	}

	@Override
	public void start() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		MetaDocument destinationParent = context.getMetaDocument();
		if (destinationParent == null) {
			getProcess().stop("Parent meta folder of destination not found");
			return;
		}

		MetaFolder parent = (MetaFolder) destinationParent;
		parent.addChildKeyPair(context.getFileNodeKeys());
		super.metaDocument = parent;

		// initialize next steps:
		// 3. update the user profile
		// 4. notify
		// TODO

		super.start();
	}
}
