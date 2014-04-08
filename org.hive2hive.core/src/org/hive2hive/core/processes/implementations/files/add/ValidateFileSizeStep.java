package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.math.BigInteger;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class ValidateFileSizeStep extends ProcessStep {

	private final File file;
	private final IFileConfiguration config;

	public ValidateFileSizeStep(File file, IFileConfiguration config) {
		this.file = file;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (file.isDirectory()) {
			// ok
			return;
		}

		// validate the file size
		if (BigInteger.valueOf(FileUtil.getFileSize(file)).compareTo(config.getMaxFileSize()) == 1) {
			throw new ProcessExecutionException("File is too large.");
		}
	}

}
