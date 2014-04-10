package org.hive2hive.core.processes.implementations.files.delete;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

public class DeleteChunksProcess extends SequentialProcess {

	private final DeleteFileProcessContext context;
	private final IDataManager dataManager;

	public DeleteChunksProcess(DeleteFileProcessContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.consumeMetaFile() == null) {
			throw new ProcessExecutionException("No meta document given.");
		}
		if (context.consumeProtectionKeys() == null) {
			throw new ProcessExecutionException("No protection keys given.");
		}

		List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();
		MetaFile metaFile = context.consumeMetaFile();

		if (metaFile.isSmall()) {
			MetaFileSmall metaSmall = (MetaFileSmall) metaFile;
			// TODO rather delete file by file than all chunks mixed
			for (FileVersion version : metaSmall.getVersions()) {
				metaChunks.addAll(version.getMetaChunks());
			}
		}

		// process composition
		for (MetaChunk metaChunk : metaChunks) {
			// TODO at a later stage, this steps could be async (parallelized)
			insertNext(new DeleteSingleChunkStep(metaChunk.getChunkId(), context.consumeProtectionKeys(),
					dataManager), this);
		}

		super.doExecute();
	}

}
