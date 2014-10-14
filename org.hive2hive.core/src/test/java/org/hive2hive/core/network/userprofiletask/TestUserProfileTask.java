package org.hive2hive.core.network.userprofiletask;

import org.hive2hive.core.H2HJUnitTest;

public class TestUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2539021366875667650L;

	private final String id;

	public TestUserProfileTask() {
		super(H2HJUnitTest.randomString());
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