package org.hive2hive.core.network;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.FSTSerializer;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.hive2hive.core.security.IH2HSerialize;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.TestFileConfiguration;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionTest extends H2HJUnitTest {

	private static IH2HSerialize serializer;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ConnectionTest.class;
		beforeClass();
		serializer = new FSTSerializer();
	}

	@Test
	public void testConnectAsInitial() {
		NetworkManager initialNode = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());

		try {
			INetworkConfiguration netConfig = NetworkConfiguration.createInitial("initial node");
			assertTrue(initialNode.connect(netConfig));
		} finally {
			initialNode.disconnect(false);
		}
	}

	@Test
	public void testConnectToOtherPeer() throws UnknownHostException {
		NetworkManager nodeA = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());
		NetworkManager nodeB = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());

		INetworkConfiguration netConfigA = NetworkConfiguration.createInitial("nodeA");
		INetworkConfiguration netConfigB = NetworkConfiguration.create("nodeB", InetAddress.getLocalHost());
		try {
			assertTrue(nodeA.connect(netConfigA));
			assertTrue(nodeB.connect(netConfigB));
		} finally {
			nodeA.disconnect(false);
			nodeB.disconnect(false);
		}
	}

	@Test
	public void testConnectDisconnect() throws UnknownHostException {
		NetworkManager nodeA = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());
		NetworkManager nodeB = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());

		INetworkConfiguration netConfigA = NetworkConfiguration.createInitial("nodeA");
		INetworkConfiguration netConfigB = NetworkConfiguration.create("nodeB", InetAddress.getLocalHost());
		try {
			nodeA.connect(netConfigA);
			nodeB.connect(netConfigB);

			assertTrue(nodeB.disconnect(false));
			assertTrue(nodeB.connect(netConfigB));
		} finally {
			nodeA.disconnect(false);
			nodeB.disconnect(false);
		}
	}

	@Test
	public void testConnectDisconnectKeepSession() throws UnknownHostException, NoPeerConnectionException,
			NoSessionException {
		NetworkManager nodeA = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());
		NetworkManager nodeB = new NetworkManager(new H2HDummyEncryption(), serializer, new TestFileConfiguration());

		INetworkConfiguration netConfigA = NetworkConfiguration.createInitial("nodeA");
		INetworkConfiguration netConfigB = NetworkConfiguration.create("nodeB", InetAddress.getLocalHost());
		try {
			nodeA.connect(netConfigA);
			nodeB.connect(netConfigB);

			UseCaseTestUtil.registerAndLogin(generateRandomCredentials(), nodeB, FileTestUtil.getTempDirectory());
			assertTrue(nodeB.disconnect(true));
			assertTrue(nodeB.connect(netConfigB));
			assertNotNull(nodeB.getSession());
		} finally {
			nodeA.disconnect(false);
			nodeB.disconnect(false);
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
