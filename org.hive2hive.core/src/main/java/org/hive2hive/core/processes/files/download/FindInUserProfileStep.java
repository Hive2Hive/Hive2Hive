package org.hive2hive.core.processes.files.download;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.GetMetaFileStep;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindInUserProfileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(FindInUserProfileStep.class);

	private final DownloadFileContext context;
	private final NetworkManager networkManager;

	public FindInUserProfileStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile userProfile = null;
		try {
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException e) {
			throw new ProcessExecutionException(e);
		}
		Index index = userProfile.getFileById(context.getFileKey());
		if (index == null) {
			throw new ProcessExecutionException("File key not found in user profile.");
		}

		context.provideIndex(index);

		// add the next steps here
		if (index.isFolder()) {
			logger.info("No download of the file needed since '{}' is a folder.", index.getFullPath());
			getParent().add(new CreateFolderStep(context, networkManager));
		} else {
			logger.info("Initalize the process for downloading file '{}'.", index.getFullPath());
			try {
				IDataManager dataManager = networkManager.getDataManager();
				getParent().add(new GetMetaFileStep(context, dataManager));
				PeerAddress ownPeerAddress = networkManager.getConnection().getPeer().getPeerAddress();
				getParent().add(new InitDownloadChunksStep(context, networkManager.getSession(), ownPeerAddress));
			} catch (NoPeerConnectionException | NoSessionException e) {
				throw new ProcessExecutionException(e);
			}
		}
	}
}
