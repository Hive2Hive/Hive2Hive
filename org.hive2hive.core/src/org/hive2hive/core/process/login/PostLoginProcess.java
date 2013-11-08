package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessContext;

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

	private final UserProfile profile;
	private Locations locations;
	private boolean isDefinedAsMaster = false;

	public PostLoginProcess(UserProfile profile, Locations currentLocations, NetworkManager networkManager) {
		super(networkManager);
		this.profile = profile;
		this.locations = currentLocations;
		
		// execution order:
		// 1. ContactPeersStep (-> PutLocationsStep)
		// 2. SynchronizeFilesStep
		
		setNextStep(new ContactPeersStep(this.locations));
	}

	public void setNewLocations(Locations newLocations){
		this.locations = newLocations;
	}
	
	public void setIsDefinedAsMaster(boolean isDefinedAsMaster) {
		this.isDefinedAsMaster = isDefinedAsMaster;
	}

	@Override
	public ProcessContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
