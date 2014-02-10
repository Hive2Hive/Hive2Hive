package org.hive2hive.core.processes.implementations.files.delete;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// check preconditions
		if (context.isFileInRoot()) {
			return;
		}
		if (context.consumeMetaDocument() == null) {
			throw new ProcessExecutionException("Parent meta folder not found.");
		}
		if (context.getDeletedIndex() == null) {
			throw new ProcessExecutionException("Child node is not given.");
		}
		if (context.getParentNode() == null) {
			throw new ProcessExecutionException("Parent node is not given.");
		}
		if (context.getParentNode().getProtectionKeys() == null) {
			throw new ProcessExecutionException("Parent protection keys are null.");
		}
		if (context.consumeMetaDocument() == null) {
			throw new ProcessExecutionException("Meta document is null.");
		}

		// update parent meta folder (delete child)
		MetaFolder parentMetaFolder = (MetaFolder) context.consumeMetaDocument();
		parentMetaFolder.removeChildKey(context.getDeletedIndex().getFilePublicKey());

		// put the meta folder
		super.doExecute();
	}

}
