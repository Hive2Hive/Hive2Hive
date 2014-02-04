package org.hive2hive.core.processes.implementations.files.delete;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

// TODO implement rollback

public class DeleteFromParentMetaStep extends PutMetaDocumentStep {

	private final DeleteFileProcessContext context;

	public DeleteFromParentMetaStep(DeleteFileProcessContext context, IDataManager dataManager) {
		super(context, context, dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// check preconditions
		if (context.isFileInRoot()) {
			return;
		}
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "Parent meta folder not found."));
			return;
		}
		if (context.getDeletedNode() == null) {
			cancel(new RollbackReason(this, "Child node is not given."));
			return;
		}
		if (context.getParentNode() == null) {
			cancel(new RollbackReason(this, "Parent node is not given."));
			return;
		}
		if (context.getParentNode().getProtectionKeys() == null) {
			cancel(new RollbackReason(this, "Parent protection keys are null."));
			return;
		}
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "Meta document is null."));
			return;
		}

		// update parent meta folder (delete child)
		MetaFolder parentMetaFolder = (MetaFolder) context.consumeMetaDocument();
		parentMetaFolder.removeChildKey(context.getDeletedNode().getFileKey());

		// put the meta folder
		super.doExecute();
	}

}