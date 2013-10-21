package org.hive2hive.core.test.network;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
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
		NetworkManager masterNode = new NetworkManager("master node");
		assertTrue(masterNode.connect());
		masterNode.disconnect();
	}
	
	@Test
	public void testConnectToOtherPeer() throws UnknownHostException{
		NetworkManager nodeA = new NetworkManager("nodeA");
		NetworkManager nodeB = new NetworkManager("nodeB");
		assertTrue(nodeA.connect());
		assertTrue(nodeB.connect(InetAddress.getLocalHost()));
		nodeA.disconnect();
		nodeB.disconnect();
	}
}
