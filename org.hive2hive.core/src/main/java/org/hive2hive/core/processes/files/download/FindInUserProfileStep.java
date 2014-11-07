package org.hive2hive.core.processes.files.download;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.core.processes.files.GetMetaFileStep;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class FindInUserProfileStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(FindInUserProfileStep.class);

	private final DownloadFileContext context;
	private final NetworkManager networkManager;

	public FindInUserProfileStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		UserProfileManager profileManager = session.getProfileManager();

		UserProfile userProfile = null;
		try {
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		Index index = userProfile.getFileById(context.getFileKey());
		if (index == null) {
			throw new ProcessExecutionException(this, "File key not found in user profile.");
		}

		context.provideIndex(index);
		setRequiresRollback(true);

		// add the next steps here
		if (index.isFolder()) {
			logger.info("No download of the file needed since '{}' is a folder.", index.getFullPath());
			getParent().add(new CreateFolderStep(context, networkManager));
		} else {
			logger.info("Initalize the process for downloading file '{}'.", index.getFullPath());
			DataManager dataManager;
			try {
				dataManager = networkManager.getDataManager();
			} catch (NoPeerConnectionException ex) {
				throw new ProcessExecutionException(this, ex);
			}
			getParent().add(new GetMetaFileStep(context, dataManager));
			getParent().add(new InitDownloadChunksStep(context, networkManager));
		}
		
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		context.provideIndex(null);
		setRequiresRollback(false);
		
		return null;
	}

}
