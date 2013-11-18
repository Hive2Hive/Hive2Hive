package org.hive2hive.core.process.delete;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.process.common.remove.RemoveProcessStep;

public class DeleteChunkStep extends RemoveProcessStep {

	private DeleteFileProcessContext context;
	private List<KeyPair> chunksToDelete;

	public DeleteChunkStep() {
		super(null, H2HConstants.FILE_CHUNK, null);

		context = (DeleteFileProcessContext) getProcess().getContext();
		chunksToDelete = new ArrayList<KeyPair>();
		if (!context.isDirectory()) {
			// no chunks to delete when directory
			MetaFile metaFile = (MetaFile) context.getMetaDocument();
			for (FileVersion version : metaFile.getVersions()) {
				chunksToDelete.addAll(version.getChunkIds());
			}
		}
	}

	private DeleteChunkStep(List<KeyPair> chunksToDelete) {
		super(null, H2HConstants.FILE_CHUNK, null);
		this.chunksToDelete = chunksToDelete;
	}

	@Override
	public void start() {
		if (chunksToDelete.isEmpty()) {
			// TODO
			// continue with next step
			return;
		}

		KeyPair toDelete = chunksToDelete.remove(0);
		nextStep = new DeleteChunkStep(chunksToDelete);
		remove(key2String(toDelete.getPublic()), H2HConstants.FILE_CHUNK);
	}
}
