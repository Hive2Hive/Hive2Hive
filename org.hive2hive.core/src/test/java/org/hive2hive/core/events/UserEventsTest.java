package org.hive2hive.core.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HWaiter;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;
import org.hive2hive.core.events.util.TestUserEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserEventsTest extends H2HJUnitTest {

	private IH2HNode node;
	private IUserManager userManager;
	private UserCredentials credentials;
	private H2HWaiter waiter = new H2HWaiter(20);
	
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserEventsTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
	
	@Before
	public void before() {
		node = H2HNode.createNode(NetworkConfiguration.create(), FileConfiguration.createDefault());
		node.connect();
		userManager = node.getUserManager();
		credentials = NetworkTestUtil.generateRandomCredentials();
	}

	@After
	public void after() {
		node.disconnect();
	}
	
	@Test
	public void registerEventsTest() throws NoPeerConnectionException {
		
		// test success
		TestUserEventListener listener = new TestUserEventListener() {
			@Override
			public void onRegisterSuccess(IRegisterEvent event) {
				assertEquals(credentials, event.getUserCredentials());
				assertNull(event.getRollbackReason());
				super.onRegisterSuccess(event);
			}
		};
		userManager.addEventListener(listener);
		userManager.register(credentials);
		while (!listener.registerSuccess) {
			waiter.tickASecond();
		}
		assertTrue(listener.registerSuccess);
		assertFalse(listener.registerFailure);
		
		// test failure (e.g. 2nd registration)
		listener = new TestUserEventListener() {
			@Override
			public void onRegisterFailure(IRegisterEvent event) {
				assertEquals(credentials, event.getUserCredentials());
				assertNotNull(event.getRollbackReason());
				super.onRegisterFailure(event);
			}
		};
		userManager.addEventListener(listener);
		userManager.register(credentials);
		while (!listener.registerFailure) {
			waiter.tickASecond();
		}
		assertTrue(listener.registerFailure);
		assertFalse(listener.registerSuccess);
	}
}
