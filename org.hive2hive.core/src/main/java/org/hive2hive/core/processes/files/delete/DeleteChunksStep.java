package org.hive2hive.core.processes.files.delete;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

public class DeleteChunksStep extends ProcessStep<Void> {

	private final DeleteFileProcessContext context;
	private final DataManager dataManager;

	public DeleteChunksStep(DeleteFileProcessContext context, DataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}
	
	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		if (context.consumeMetaFile() == null) {
			throw new ProcessExecutionException(this, "No meta document given.");
		}
		if (context.consumeProtectionKeys() == null) {
			throw new ProcessExecutionException(this, "No protection keys given.");
		}

		List<MetaChunk> metaChunks = new ArrayList<MetaChunk>();
		BaseMetaFile metaFile = context.consumeMetaFile();

		if (metaFile.isSmall()) {
			MetaFileSmall metaSmall = (MetaFileSmall) metaFile;
			// TODO rather delete file by file than all chunks mixed
			for (FileVersion version : metaSmall.getVersions()) {
				metaChunks.addAll(version.getMetaChunks());
			}
		}

		// process composition
		List<IProcessComponent<?>> parentComponents = new ArrayList<>(getParent().getComponents());
		int index = parentComponents.indexOf(this) + 1;

		for (MetaChunk metaChunk : metaChunks) {
			// TODO at a later stage, this steps could be async (parallelized)
			
			IProcessComponent<?> step = new DeleteSingleChunkStep(metaChunk.getChunkId(), context.consumeProtectionKeys(),
					dataManager);
			getParent().add(index++, step);
		}
		
		return null;
	}

}
