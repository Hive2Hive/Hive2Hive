package org.hive2hive.core.network;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.security.FSTSerializer;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionTest extends H2HJUnitTest {

	private static FSTSerializer serializer;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ConnectionTest.class;
		beforeClass();
		serializer = new FSTSerializer();
	}

	@Test
	public void testConnectAsInitial() {
		NetworkManager initialNode = new NetworkManager(new H2HDummyEncryption(), serializer, new EventBus(),
				FileConfiguration.createDefault());

		try {
			INetworkConfiguration netConfig = NetworkConfiguration.createInitial("initial node");
			assertTrue(initialNode.connect(netConfig));
		} finally {
			initialNode.disconnect();
		}
	}

	@Test
	public void testConnectToOtherPeer() throws UnknownHostException {
		NetworkManager nodeA = new NetworkManager(new H2HDummyEncryption(), serializer, new EventBus(),
				FileConfiguration.createDefault());
		NetworkManager nodeB = new NetworkManager(new H2HDummyEncryption(), serializer, new EventBus(),
				FileConfiguration.createDefault());

		INetworkConfiguration netConfigA = NetworkConfiguration.createInitial("nodeA");
		INetworkConfiguration netConfigB = NetworkConfiguration.create("nodeB", InetAddress.getLocalHost());
		try {
			assertTrue(nodeA.connect(netConfigA));
			assertTrue(nodeB.connect(netConfigB));
		} finally {
			nodeA.disconnect();
			nodeB.disconnect();
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
