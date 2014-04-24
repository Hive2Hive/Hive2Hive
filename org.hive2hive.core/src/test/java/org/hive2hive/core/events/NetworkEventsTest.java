package org.hive2hive.core.events;

import static org.junit.Assert.assertTrue;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.events.util.TestNetworkEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NetworkEventsTest extends H2HJUnitTest {
		
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = NetworkEventsTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
	
	@Test
	public void eventListenerTest() {
		
		IH2HNode node = H2HNode.createNode(NetworkConfiguration.create(), FileConfiguration.createDefault());
		
		TestNetworkEventListener listener = new TestNetworkEventListener();
		node.addEventListener(listener);
		
		node.connect();
		
		assertTrue(listener.connectionSucceeded || listener.connectionFailed);
		assertTrue(!listener.disconnectionSucceeded && !listener.disconnectionFailed);
		listener.reset();
		
		node.disconnect();
		assertTrue(listener.disconnectionSucceeded || listener.disconnectionFailed);
		assertTrue(!listener.connectionSucceeded && !listener.connectionFailed);
		listener.reset();
	}

}
