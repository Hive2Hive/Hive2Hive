package org.hive2hive.core.network.userprofiletask;

import java.security.KeyPair;

import org.hive2hive.core.H2HJUnitTest;

public class TestUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2539021366875667650L;

	private final String id;

	public TestUserProfileTask(KeyPair protectionKeys) {
		super(H2HJUnitTest.randomString(), protectionKeys);
		this.id = H2HJUnitTest.randomString();
	}

	public String getId() {
		return id;
	}

	@Override
	public void start() {
		// do nothing
	}

}