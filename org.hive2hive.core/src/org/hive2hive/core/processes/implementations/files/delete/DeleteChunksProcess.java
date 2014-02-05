package org.hive2hive.core.processes.implementations.files.delete;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

// TODO this class should be merged with org.hive2hive.processes.implementations.files.update.DeleteChunksStep
// TODO this step is only needed for files, context.isDirectory is therefore not needed
public class DeleteChunksProcess extends SequentialProcess {

	private final DeleteFileProcessContext context;
	private final IDataManager dataManager;

	public DeleteChunksProcess(DeleteFileProcessContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
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

		// if file, enlist all chunks to delete
		if (!context.isDirectory()) {

			List<KeyPair> chunkKeys = new ArrayList<KeyPair>();
			MetaFile metaFile = (MetaFile) context.consumeMetaDocument();

			// TODO rather delete file by file than all chunks mixed
			for (FileVersion version : metaFile.getVersions()) {
				chunkKeys.addAll(version.getChunkKeys());
			}

			// process composition
			for (KeyPair keyPair : chunkKeys) {
				// TODO at a later stage, this steps could be async (parallelized)
				add(new DeleteSingleChunkStep(keyPair.getPublic(), context.consumeProtectionKeys(),
						dataManager));
			}
		}

		super.doExecute();
	}

}
