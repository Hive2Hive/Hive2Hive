package org.hive2hive.core.processes.logout;

import java.io.IOException;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.serializer.IH2HSerialize;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WritePersistentStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(WritePersistentStep.class);

	private final IFileAgent fileAgent;
	private final PublicKeyManager keyManager;
	private final IH2HSerialize serializer;

	public WritePersistentStep(IFileAgent fileAgent, PublicKeyManager keyManager, IH2HSerialize serializer) {
		this.serializer = serializer;
		this.fileAgent = fileAgent;
		this.keyManager = keyManager;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			// write the current state to a meta file
			FileUtil.writePersistentMetaData(fileAgent, keyManager, serializer);
		} catch (IOException ex) {
			// it's not mandatory, but recommended. Thus we don't rollback the logout process here
			logger.error("Meta data could not be persisted.", ex);
		}
		return null;
	}

}
