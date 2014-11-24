package org.hive2hive.core.processes.login;

import java.security.PublicKey;
import java.util.Map;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.PersistentMetaData;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class SessionCreationStep extends ProcessStep<Void> {

	private final LoginProcessContext context;
	private final NetworkManager networkManager;

	public SessionCreationStep(LoginProcessContext context, NetworkManager networkManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		H2HSession session;
		try {
			SessionParameters params = context.consumeSessionParameters();

			// create user profile manager
			UserProfileManager userProfileManager = new UserProfileManager(networkManager.getDataManager(),
					context.consumeUserCredentials());
			params.setUserProfileManager(userProfileManager);

			// load user profile
			UserProfile userProfile = userProfileManager.readUserProfile();
			context.provideUserProfile(userProfile);

			// create the locations manager
			VersionManager<Locations> locationsManager = new VersionManager<Locations>(networkManager.getDataManager(),
					context.consumeUserId(), H2HConstants.USER_LOCATIONS);
			params.setLocationsManager(locationsManager);

			// get the persistently cached items
			PersistentMetaData metaData = FileUtil.readPersistentMetaData(params.getFileAgent(), networkManager
					.getDataManager().getSerializer());

			// create the key manager
			PublicKeyManager keyManager = new PublicKeyManager(userProfile.getUserId(), userProfile.getEncryptionKeys(),
					networkManager.getDataManager());

			// read eventually cached keys and add them to the key manager
			Map<String, PublicKey> publicKeyCache = metaData.getPublicKeyCache();
			for (String userId : publicKeyCache.keySet()) {
				keyManager.putPublicKey(userId, publicKeyCache.get(userId));
			}
			params.setKeyManager(keyManager);

			// create the download manager
			DownloadManager downloadManager = networkManager.getDownloadManager();

			// read the cached downloads and add them to the download manager
			for (BaseDownloadTask task : metaData.getDownloads()) {
				task.reinitializeAfterDeserialization(networkManager.getEventBus(), keyManager);
				downloadManager.submit(task);
			}
			params.setDownloadManager(downloadManager);

			// create session
			session = new H2HSession(params);
		} catch (NoPeerConnectionException ex) {
			throw new ProcessExecutionException(this, ex, "Session could not be created.");
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		// set session
		networkManager.setSession(session);
		setRequiresRollback(true);

		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		// invalidate the session
		networkManager.setSession(null);
		setRequiresRollback(false);
		return null;
	}

}
