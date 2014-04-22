package org.hive2hive.core.network.userprofiletask;

import org.hive2hive.core.network.NetworkTestUtil;

public class TestUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2539021366875667650L;

	private final String id;

	public TestUserProfileTask() {
		super(NetworkTestUtil.randomString());
		this.id = NetworkTestUtil.randomString();
	}

	public String getId() {
		return id;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

}
