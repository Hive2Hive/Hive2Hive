package org.hive2hive.core.process.login;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
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
 * @author Nico, Christian
 * 
 */
public class PostLoginProcess extends Process {

	private final PostLoginProcessContext context;

	public PostLoginProcess(Locations locations, NetworkManager networkManager) throws NoSessionException {
		super(networkManager);

		// execution order:
		// 1. ContactPeersStep (-> PutLocationsStep)
		// 2. SynchronizeFilesStep
		// if elected master:
		// 4. HandleUserMessageQueueStep
		H2HSession session = networkManager.getSession();
		context = new PostLoginProcessContext(this, session.getProfileManager(), locations,
				session.getFileManager(), session.getFileConfiguration());

		setNextStep(new ContactPeersStep());
	}

	@Override
	public PostLoginProcessContext getContext() {
		return context;
	}
}
