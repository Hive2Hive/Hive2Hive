package org.hive2hive.core.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILogoutEvent;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;
import org.hive2hive.core.events.util.TestUserEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserEventsTest extends H2HJUnitTest {

	private IH2HNode node;
	private IUserManager userManager;
	private UserCredentials credentials;
	private Path rootPath;
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
		rootPath = FileTestUtil.getTempDirectory().toPath();
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
		
		// test failure (i.e., 2nd registration)
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
	
	@Test
	public void loginEventsTest() throws NoPeerConnectionException, InterruptedException {
		
		// test failure (i.e., login before register)
		TestUserEventListener listener = new TestUserEventListener() {
			@Override
			public void onLoginFailure(ILoginEvent event) {
				assertEquals(credentials, event.getUserCredentials());
				assertEquals(rootPath, event.getRootPath());
				assertNotNull(event.getRollbackReason());
				super.onLoginFailure(event);
			}
		};
		userManager.addEventListener(listener);
		userManager.login(credentials, rootPath);
		while (!listener.loginFailure) {
			waiter.tickASecond();
		}
		assertTrue(listener.loginFailure);
		assertFalse(listener.loginSuccess);
		
		// test success
		userManager.register(credentials).await();
		
		listener = new TestUserEventListener() {
			@Override
			public void onLoginSuccess(ILoginEvent event) {
				assertEquals(credentials, event.getUserCredentials());
				assertEquals(rootPath, event.getRootPath());
				assertNull(event.getRollbackReason());
				super.onLoginSuccess(event);
			}
		};
		userManager.addEventListener(listener);
		userManager.login(credentials, rootPath);
		while (!listener.loginSuccess) {
			waiter.tickASecond();
		}
		assertTrue(listener.loginSuccess);
		assertFalse(listener.loginFailure);
		
	}
	
	@Test
	public void logoutEventsTest() throws NoPeerConnectionException, InterruptedException, NoSessionException {
		
		// TODO test failure
		
		// test success
		userManager.register(credentials).await();
		userManager.login(credentials, rootPath).await();
		
		TestUserEventListener listener = new TestUserEventListener() {
			@Override
			public void onLogoutSuccess(ILogoutEvent event) {
				assertEquals(credentials, event.getUserCredentials());
				assertNull(event.getRollbackReason());
				super.onLogoutSuccess(event);
			}
		};
		userManager.addEventListener(listener);
		userManager.logout();
		while (!listener.logoutSuccess) {
			waiter.tickASecond();
		}
		assertTrue(listener.logoutSuccess);
		assertFalse(listener.logoutFailure);
		
	}
}
