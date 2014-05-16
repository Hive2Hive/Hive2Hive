package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.math.BigInteger;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the file size
 * 
 * @author Nico
 * 
 */
public class ValidateFileSizeStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(ValidateFileSizeStep.class);

	private final AddFileProcessContext context;

	public ValidateFileSizeStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.consumeFile();
		if (file.isDirectory()) {
			// ok
			return;
		}
		
		// validate the file size
		IFileConfiguration config = context.consumeFileConfiguration();
		if (BigInteger.valueOf(FileUtil.getFileSize(file)).compareTo(config.getMaxFileSize()) == 1) {
			logger.debug("File " + file.getName() + " is a 'large file'.");
			context.setLargeFile(true);
		} else {
			logger.debug("File " + file.getName() + " is a 'small file'.");
			context.setLargeFile(false);
		}
	}
}
