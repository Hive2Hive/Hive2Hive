package org.hive2hive.core.processes.implementations.files.delete;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStep;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

public class DeleteMetaFileStep extends BaseRemoveProcessStep {

	private final DeleteFileProcessContext context;

	public DeleteMetaFileStep(DeleteFileProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.consumeMetaFile() == null) {
			throw new ProcessExecutionException("No meta file given.");
		}
		if (context.consumeProtectionKeys() == null) {
			throw new ProcessExecutionException("No protection keys given.");
		}
		if (context.consumeEncryptedMetaFile() == null) {
			throw new ProcessExecutionException("No encrypted meta file given.");
		}

		try {
			remove(context.consumeMetaFile().getId(), H2HConstants.META_FILE,
					context.consumeEncryptedMetaFile(), context.consumeProtectionKeys());
		} catch (RemoveFailedException e) {
			throw new ProcessExecutionException("Remove of meta document failed.", e);
		}
	}

}
