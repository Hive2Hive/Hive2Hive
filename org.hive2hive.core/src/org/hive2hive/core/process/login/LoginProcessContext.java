package org.hive2hive.core.process.login;

import java.security.PublicKey;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.IGetPublicKeyContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.context.ProcessContext;

public final class LoginProcessContext extends ProcessContext implements IGetLocationsContext,
		IGetUserProfileContext, IGetPublicKeyContext {

	private Locations locations;
	private UserProfile userProfile;
	private PublicKey publicKey;
	
	private H2HSession session;
	
	public LoginProcessContext(LoginProcess loginProcess) {
		super(loginProcess);
	}

	@Override
	public void setLocation(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations getLocations() {
		return locations;
	}

	@Override
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

	@Override
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public H2HSession getSession() {
		return session;
	}

	public void setSession(H2HSession session) {
		this.session = session;
	}
}
