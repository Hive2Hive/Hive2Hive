package org.hive2hive.core.processes.implementations.files.delete;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStep;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

public class DeleteMetaDocumentStep extends BaseRemoveProcessStep {

	private final DeleteFileProcessContext context;

	public DeleteMetaDocumentStep(DeleteFileProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.consumeMetaDocument() == null) {
			throw new ProcessExecutionException("No meta document given.");
		}
		if (context.consumeProtectionKeys() == null) {
			throw new ProcessExecutionException("No protection keys given.");
		}
		if (context.getEncryptedMetaDocument() == null) {
			throw new ProcessExecutionException("No encrypted meta document given.");
		}

		try {
			remove(context.consumeMetaDocument().getId(), H2HConstants.META_DOCUMENT,
					context.getEncryptedMetaDocument(), context.consumeProtectionKeys());
		} catch (RemoveFailedException e) {
			throw new ProcessExecutionException("Remove of meta document failed.", e);
		}
	}

}
