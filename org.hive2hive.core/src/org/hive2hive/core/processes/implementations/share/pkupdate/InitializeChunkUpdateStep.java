package org.hive2hive.core.processes.implementations.share.pkupdate;

import java.security.KeyPair;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.MetaDocumentPKUpdateContext;

public class InitializeChunkUpdateStep extends ProcessStep {

	private MetaDocumentPKUpdateContext context;

	public InitializeChunkUpdateStep(MetaDocumentPKUpdateContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();
		if (metaFile == null) {
			throw new ProcessExecutionException("Meta File not found");
		}

		for (FileVersion version : metaFile.getVersions()) {
			for (KeyPair chunkKey : version.getChunkKeys()) {
				// TODO build process to update the chunks protection key
			}
		}
	}

}
