package org.hive2hive.core.events;

import org.hive2hive.core.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
	
	/*@Test
	public void eventListenerTest() {
		
		IH2HNode node = H2HNode.createNode(NetworkConfiguration.createInitial(), FileConfiguration.createDefault());
		
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
	}*/

}
