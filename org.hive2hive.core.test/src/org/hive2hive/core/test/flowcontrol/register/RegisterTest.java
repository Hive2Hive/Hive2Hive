package org.hive2hive.core.test.flowcontrol.register;

import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegisterTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RegisterTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testCheckIfProfileExistsStepRollback() {
		String userId = "a user id";
		String password = "a password";

		NetworkManager node = network.get(0);

		node.putGlobal(userId, H2HConstants.USER_PROFILE, new UserProfile(userId, null, null));

		RegisterProcess process = new RegisterProcess(userId, password, node);
		RegisterProcessTestListener listener = new RegisterProcessTestListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasExecuted());
	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	private class RegisterProcessTestListener implements IProcessListener {

		public boolean executed = false;

		public boolean hasExecuted() {
			return executed;
		}

		@Override
		public void onSuccess() {
			// register process shouldn't success in this case;
		}

		@Override
		public void onFail(String reason) {
			executed = true;
		}

	}
}
