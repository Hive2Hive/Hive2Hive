package org.hive2hive.processes.test.implementations.register;

import java.util.List;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.processes.framework.concretes.ProcessListener;
import org.hive2hive.processes.framework.decorators.AsyncComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.register.RegisterProcess;
import org.hive2hive.processes.implementations.register.RegisterProcessContext;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegisterProcessTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int NETWORK_SIZE = 2;
	
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RegisterProcessTest.class;
		beforeClass();
	}
	
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		
		network = NetworkTestUtil.createNetwork(NETWORK_SIZE);
	}
	
	@Test
	public void testRegisterProcessSuccess() throws InvalidProcessStateException {
		
		NetworkManager client = network.get(0);
		NetworkManager otherClient = network.get(1);
		
		UserCredentials credentials = NetworkTestUtil.generateRandomCredentials();
		
		RegisterProcessContext context = new RegisterProcessContext(client);
		ProcessListener listener = new ProcessListener();
		
		RegisterProcess process = new RegisterProcess(credentials, context);
		AsyncComponent asyncProcess = new AsyncComponent(process);
		
		asyncProcess.attachListener(listener);
		asyncProcess.start();
		
		H2HWaiter waiter = new H2HWaiter(30);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}
}
