package org.hive2hive.core.test.processes.implementations.userprofiletask;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.test.network.NetworkTestUtil;

public class TestUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2539021366875667650L;

	private final String id;

	public TestUserProfileTask() {
		super();
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
