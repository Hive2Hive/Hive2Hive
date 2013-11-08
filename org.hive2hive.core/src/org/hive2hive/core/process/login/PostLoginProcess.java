package org.hive2hive.core.process.login;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * This process does all long-running tasks necessary after the login:
 * <ul>
 * <li>Contact all peers in the locations map and update them</li>
 * <li>Synchronize the files changed on current disk and in user profile</li>
 * <li>If master, handle the user message queue</li>
 * </ul>
 * 
 * @author Nico
 * 
 */
public class PostLoginProcess extends Process {

	private final PostLoginProcessContext context;

	public PostLoginProcess(UserProfile profile, Locations currentLocations, NetworkManager networkManager,
			FileManager fileManager) {
		super(networkManager);
		context = new PostLoginProcessContext(this, profile, currentLocations, fileManager);

		// execution order:
		// 1. ContactPeersStep (-> PutLocationsStep)
		// 2. SynchronizeFilesStep
		setNextStep(new ContactPeersStep(currentLocations));
	}

	@Override
	public PostLoginProcessContext getContext() {
		return context;
	}
}
