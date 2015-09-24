package org.hive2hive.core.events;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.events.framework.interfaces.IUserEventListener;
import org.hive2hive.core.events.framework.interfaces.user.IUserLoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.IUserLogoutEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserEventsTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserEventsTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	@Test
	public void loginLogoutEventTest() throws NoPeerConnectionException, NoSessionException, IOException {
		List<NetworkManager> network = NetworkTestUtil.createNetwork(2);

		// register an event listener at each node
		TestUserEventListener listener0 = new TestUserEventListener();
		network.get(0).getEventBus().subscribe(listener0);
		TestUserEventListener listener1 = new TestUserEventListener();
		network.get(1).getEventBus().subscribe(listener1);

		UserCredentials credentials = generateRandomCredentials("username");
		UseCaseTestUtil.register(credentials, network.get(0));

		assertEquals(0, listener0.loginEvents.size());
		assertEquals(0, listener1.loginEvents.size());

		UseCaseTestUtil.login(credentials, network.get(0), tempFolder.newFolder());

		// there should be still no event
		assertEquals(0, listener0.loginEvents.size());
		assertEquals(0, listener1.loginEvents.size());

		UseCaseTestUtil.login(credentials, network.get(1), tempFolder.newFolder());

		// the first client should now have a login event
		assertEquals(1, listener0.loginEvents.size());
		assertEquals("username", listener0.loginEvents.get(0).getCurrentUser());
		assertEquals(network.get(1).getConnection().getPeer().peerAddress(), listener0.loginEvents.get(0).getClientAddress());
		assertEquals(0, listener1.loginEvents.size());

		UseCaseTestUtil.logout(network.get(0));

		// the second client should now have a logout event
		assertEquals(0, listener0.logoutEvents.size());
		assertEquals(1, listener1.logoutEvents.size());
		assertEquals("username", listener1.logoutEvents.get(0).getCurrentUser());
		assertEquals(network.get(0).getConnection().getPeer().peerAddress(), listener1.logoutEvents.get(0)
				.getClientAddress());

	}

	private class TestUserEventListener implements IUserEventListener {

		final List<IUserLoginEvent> loginEvents = new ArrayList<IUserLoginEvent>();
		final List<IUserLogoutEvent> logoutEvents = new ArrayList<IUserLogoutEvent>();

		@Override
		@Handler
		public void onClientLogin(IUserLoginEvent loginEvent) {
			loginEvents.add(loginEvent);
		}

		@Override
		@Handler
		public void onClientLogout(IUserLogoutEvent logoutEvent) {
			logoutEvents.add(logoutEvent);
		}
	}
}
