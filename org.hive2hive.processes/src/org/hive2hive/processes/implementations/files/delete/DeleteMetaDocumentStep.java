package org.hive2hive.processes.implementations.files.delete;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BaseRemoveProcessStep;
import org.hive2hive.processes.implementations.context.DeleteFileProcessContext;

public class DeleteMetaDocumentStep extends BaseRemoveProcessStep {

	private final DeleteFileProcessContext context;

	public DeleteMetaDocumentStep(DeleteFileProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "No meta document given."));
			return;
		}
		if (context.consumeProtectionKeys() == null) {
			cancel(new RollbackReason(this, "No protection keys given."));
			return;
		}
		if (context.getEncryptedMetaDocument() == null) {
			cancel(new RollbackReason(this, "No encrypted meta document given."));
			return;
		}

		try {
			remove(context.consumeMetaDocument().getId(), H2HConstants.META_DOCUMENT,
					context.getEncryptedMetaDocument(), context.consumeProtectionKeys());
		} catch (RemoveFailedException e) {
			cancel(new RollbackReason(this, "Remove of meta document failed."));
		}
	}

}
