package org.hive2hive.core.process.login;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;

/**
 * This process does all long-running tasks necessary after the login:
 * <ul>
 * <li>Contact all peers in the locations map and update them</li>
 * <li>Synchronize the files changed on current disk and in user profile</li>
 * <li>If master, handle the user message queue</li>
 * </ul>
 * 
 * @author Nico, Christian
 * 
 */
public class PostLoginProcess extends Process {

	private final PostLoginProcessContext context;

	public PostLoginProcess(UserProfileManager profileManager, Locations currentLocations,
			NetworkManager networkManager, FileManager fileManager, IH2HFileConfiguration fileConfig) {
		super(networkManager);

		// execution order:
		// 1. ContactPeersStep (-> PutLocationsStep)
		// 2. SynchronizeFilesStep
		// if elected master:
		// 4. HandleUserMessageQueueStep
		context = new PostLoginProcessContext(this, profileManager, currentLocations, fileManager, fileConfig);

		setNextStep(new ContactPeersStep());
	}

	public PostLoginProcess(H2HSession session, Locations locations, NetworkManager networkManager) {
		this(session.getProfileManager(), locations, networkManager, session.getFileManager(), session
				.getFileConfiguration());
	}

	@Override
	public PostLoginProcessContext getContext() {
		return context;
	}
}
