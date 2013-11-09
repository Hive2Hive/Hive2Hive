package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public class PostLoginProcessContext extends ProcessContext {

	private final UserProfile profile;
	private Locations locations;
	private boolean isElectedMaster = false;
	
	private final GetUserMessageQueueStep umQueueStep;
	
	public PostLoginProcessContext(PostLoginProcess postLoginProcess, UserProfile profile, Locations currentLocations, GetUserMessageQueueStep umQueueStep) {
		super(postLoginProcess);
		
		this.profile = profile;
		this.locations = currentLocations;
		
		this.umQueueStep = umQueueStep;
	}
	
	public void setNewLocations(Locations newLocations) {
		this.locations = newLocations;
	}
	
	/**
	 * Defines whether client is elected as master client.
	 * @param isDefinedAsMaster True, if this client is the master client.
	 */
	public void setIsElectedMaster(boolean isDefinedAsMaster) {
		this.isElectedMaster = isDefinedAsMaster;
	}
	
	/**
	 * Gets whether this client is elected as master client.
	 * @return True, if this client is the master client.
	 */
	public boolean getIsDefinedAsMaster() {
		return isElectedMaster;
	}
	
	public GetUserMessageQueueStep getGetUserMessageQueueStep() {
		return umQueueStep;
	}
}