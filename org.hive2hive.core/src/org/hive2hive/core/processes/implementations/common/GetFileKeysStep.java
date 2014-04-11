package org.hive2hive.core.processes.implementations.common;

import java.io.File;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets the file keys (and their protection keys)
 * 
 * @author Nico
 * 
 */
public class GetFileKeysStep extends ProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(GetFileKeysStep.class);

	private final File file;
	private final IProvideProtectionKeys protectionContext;
	private final IProvideKeyPair keyPairContext;
	private final H2HSession session;

	public GetFileKeysStep(File file, IProvideProtectionKeys protectionContext,
			IProvideKeyPair keyPairContext, H2HSession session) {
		this.file = file;
		this.protectionContext = protectionContext;
		this.keyPairContext = keyPairContext;
		this.session = session;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// file node can be null or already present
		logger.info("Getting the corresponding file node for file '{}'.", file.getName());

		// file node is null, first look it up in the user profile
		UserProfile profile = null;
		try {
			profile = session.getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(e);
		}

		Index fileNode = profile.getFileByPath(file, session.getRoot());
		if (fileNode == null) {
			throw new ProcessExecutionException(
					"File does not exist in user profile. Consider uploading a new file.");
		}

		// set the corresponding content protection keys
		protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
		keyPairContext.provideKeyPair(fileNode.getFileKeys());
	}
}
