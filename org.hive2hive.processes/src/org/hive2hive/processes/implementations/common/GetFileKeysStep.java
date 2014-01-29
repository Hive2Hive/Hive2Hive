package org.hive2hive.processes.implementations.common;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.interfaces.IProvideKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

/**
 * Gets the file keys (and their protection keys)
 * 
 * @author Nico
 * 
 */
public class GetFileKeysStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetFileKeysStep.class);

	private final File file;
	private final NetworkManager networkManager;
	private final IProvideProtectionKeys protectionContext;
	private final IProvideKeyPair keyPairContext;

	public GetFileKeysStep(File file, IProvideProtectionKeys protectionContext,
			IProvideKeyPair keyPairContext, NetworkManager networkManager) {
		this.file = file;
		this.protectionContext = protectionContext;
		this.keyPairContext = keyPairContext;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// file node can be null or already present
		logger.info(String.format("Getting the corresponding file node for file '%s'.", file.getName()));

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e1) {
			cancel(new RollbackReason(this, "No session"));
			return;
		}

		// file node is null, first look it up in the user profile
		UserProfile profile = null;
		try {
			profile = session.getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
			return;
		}

		FileTreeNode fileNode = profile.getFileByPath(file, session.getFileManager());
		if (fileNode == null) {
			cancel(new RollbackReason(this,
					"File does not exist in user profile. Consider uploading a new file."));
			return;
		}

		// set the corresponding content protection keys
		protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
		keyPairContext.provideKeyPair(fileNode.getKeyPair());
	}
}
