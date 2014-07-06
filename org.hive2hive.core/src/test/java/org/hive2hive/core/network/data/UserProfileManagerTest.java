package org.hive2hive.core.network.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link UserProfileManager} which handles concurrency when editing a user profile with multiple
 * processes. Many combinations of get, put and modify are tested.
 * 
 * @author Nico
 * 
 */
public class UserProfileManagerTest extends H2HJUnitTest {

	private UserCredentials userCredentials;
	private NetworkManager client;

	private enum Operation {
		PUT,
		GET,
		MODIFY
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileManagerTest.class;
		beforeClass();
	}

	@Before
	public void setup() throws NoPeerConnectionException {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = NetworkTestUtil.createNetwork(1).get(0);
		UseCaseTestUtil.register(userCredentials, client);
	}

	@Test
	public void testGetOnly() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.GET, Operation.GET, Operation.GET, Operation.GET);
	}

	@Test
	public void testPutSingle() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.GET, Operation.PUT, Operation.GET);
	}

	@Test
	public void testPutMultiple() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.PUT, Operation.PUT, Operation.PUT);
	}

	@Test
	public void testModifySingle() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.PUT, Operation.MODIFY, Operation.PUT);
	}

	@Test
	public void testModifyMultiple() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.MODIFY, Operation.MODIFY, Operation.MODIFY);
	}

	@Test
	public void testAllMixed() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		executeProcesses(Operation.PUT, Operation.GET, Operation.MODIFY, Operation.GET, Operation.PUT, Operation.MODIFY,
				Operation.MODIFY, Operation.GET, Operation.GET, Operation.PUT, Operation.PUT, Operation.GET);
	}

	/**
	 * Transforms the operations into a set of processes and starts them all. The processes are started with a
	 * small delay, but in the same order as the parameters. The method blocks until all processes are done.
	 * 
	 * @throws InvalidProcessStateException
	 * @throws NoPeerConnectionException
	 */
	private void executeProcesses(Operation... operations) throws GetFailedException, InterruptedException,
			InvalidProcessStateException, NoPeerConnectionException {
		UserProfileManager manager = new UserProfileManager(client.getDataManager(), userCredentials);

		List<IProcessComponent> processes = new ArrayList<IProcessComponent>(operations.length);
		List<TestProcessComponentListener> listeners = new ArrayList<TestProcessComponentListener>(operations.length);

		for (int i = 0; i < operations.length; i++) {
			TestUserProfileStep proc = new TestUserProfileStep(manager, operations[i]);
			TestProcessComponentListener listener = new TestProcessComponentListener();
			proc.attachListener(listener);

			processes.add(new AsyncComponent(proc));
			listeners.add(listener);
		}

		// start, but not all at the same time
		for (IProcessComponent process : processes) {
			process.start();
			// sleep for random time
			Thread.sleep(Math.abs(new Random().nextLong() % 100));
		}

		H2HWaiter waiter = new H2HWaiter(20);
		boolean allFinished;
		do {
			waiter.tickASecond();
			allFinished = true;

			for (TestProcessComponentListener listener : listeners) {
				allFinished &= listener.hasSucceeded();
			}
		} while (!allFinished);
	}

	@After
	public void shutdown() {
		client.disconnect();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	/**
	 * Gets the user profile using the {@link UserProfileManager}.
	 * 
	 * @author Nico
	 * 
	 */
	private class TestUserProfileStep extends ProcessStep {

		private final UserProfileManager profileManager;
		private final Operation operation;

		/**
		 * 
		 * @param profileManager
		 * @param put if true, it performs a put operation
		 * @param modify if true, it does a modification
		 */
		public TestUserProfileStep(UserProfileManager profileManager, Operation operation) {
			this.profileManager = profileManager;
			this.operation = operation;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				UserProfile userProfile = profileManager.getUserProfile(getID(), operation == Operation.PUT);

				if (operation == Operation.MODIFY) {
					new FolderIndex(userProfile.getRoot(), null, NetworkTestUtil.randomString());
				}

				if (operation == Operation.PUT) {
					profileManager.readyToPut(userProfile, getID());
				}

			} catch (GetFailedException | PutFailedException e) {
				throw new ProcessExecutionException(e);
			}
		}
	}
}
