package org.hive2hive.core.test.network;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionTest extends H2HJUnitTest{

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ConnectionTest.class;
		beforeClass();
	}
	
	@Test
	public void testConnectAsMaster(){
		
		INetworkConfiguration netConfig = NetworkConfiguration.create("master node");
		
		NetworkManager masterNode = new NetworkManager(netConfig);
		assertTrue(masterNode.connect());
		masterNode.disconnect();
	}
	
	@Test
	public void testConnectToOtherPeer() throws UnknownHostException{
		
		INetworkConfiguration netConfigA = NetworkConfiguration.create("nodeA");
		INetworkConfiguration netConfigB = NetworkConfiguration.create("nodeB", InetAddress.getLocalHost());
		
		NetworkManager nodeA = new NetworkManager(netConfigA);
		NetworkManager nodeB = new NetworkManager(netConfigB);
		assertTrue(nodeA.connect());
		assertTrue(nodeB.connect());
		nodeA.disconnect();
		nodeB.disconnect();
	}
	
	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
