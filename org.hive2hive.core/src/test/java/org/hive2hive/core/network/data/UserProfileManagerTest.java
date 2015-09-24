package org.hive2hive.core.network.data;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.H2HWaiter;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestProcessComponentListener;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link UserProfileManager} which handles concurrency when editing a user profile with multiple
 * processes. Many combinations of get, put and modify are tested.
 * 
 * @author Nico, Seppi
 */
public class UserProfileManagerTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final Random rnd = new Random();

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
		network = NetworkTestUtil.createNetwork(DEFAULT_NETWORK_SIZE);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	@Before
	public void setup() throws NoPeerConnectionException, IOException, GeneralSecurityException {
		userCredentials = generateRandomCredentials();
		client = NetworkTestUtil.getRandomNode(network);

		// create an user profile
		UserProfile userProfile = new UserProfile(userCredentials.getUserId(),
				generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS), generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION));
		// encrypt it (fake encryption)
		EncryptedNetworkContent encrypted = client.getDataManager().getEncryption().encryptAES(userProfile, null);
		encrypted.setVersionKey(userProfile.getVersionKey());
		encrypted.generateVersionKey();

		// upload user profile, avoids register step
		IParameters parameters = new Parameters().setLocationKey(userCredentials.getProfileLocationKey())
				.setContentKey(H2HConstants.USER_PROFILE).setVersionKey(encrypted.getVersionKey())
				.setNetworkContent(encrypted).setProtectionKeys(userProfile.getProtectionKeys())
				.setTTL(userProfile.getTimeToLive());
		client.getDataManager().put(parameters);
	}

	@Test
	public void testGetOnly() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.GET, Operation.GET, Operation.GET, Operation.GET);
	}

	@Test
	public void testPutSingle() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.GET, Operation.PUT, Operation.GET);
	}

	@Test
	public void testPutMultiple() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.PUT, Operation.PUT, Operation.PUT);
	}

	@Test
	public void testModifySingle() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.PUT, Operation.MODIFY, Operation.PUT);
	}

	@Test
	public void testModifyMultiple() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.MODIFY, Operation.MODIFY, Operation.MODIFY);
	}

	@Test
	public void testAllMixed() throws GetFailedException, InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		executeProcesses(Operation.PUT, Operation.GET, Operation.MODIFY, Operation.GET, Operation.PUT, Operation.MODIFY,
				Operation.MODIFY, Operation.GET, Operation.GET, Operation.PUT, Operation.PUT, Operation.GET);
	}

	/**
	 * Transforms the operations into a set of processes and starts them all. The processes are started with a
	 * small delay, but in the same order as the parameters. The method blocks until all processes are done.
	 * 
	 * @throws ProcessExecutionException
	 */
	private void executeProcesses(Operation... operations) throws GetFailedException, InterruptedException,
			InvalidProcessStateException, NoPeerConnectionException, ProcessExecutionException {
		UserProfileManager manager = new UserProfileManager(client.getDataManager(), userCredentials);

		List<IProcessComponent<Future<Void>>> processes = new ArrayList<IProcessComponent<Future<Void>>>(operations.length);
		List<TestProcessComponentListener> listeners = new ArrayList<TestProcessComponentListener>(operations.length);

		for (int i = 0; i < operations.length; i++) {
			TestUserProfileStep proc = new TestUserProfileStep(manager, operations[i]);
			TestProcessComponentListener listener = new TestProcessComponentListener();
			proc.attachListener(listener);

			processes.add(new AsyncComponent<>(proc));
			listeners.add(listener);
		}

		// start, but not all at the same time
		for (IProcessComponent<Future<Void>> process : processes) {
			process.execute();
			// sleep for random time
			Thread.sleep(Math.abs(rnd.nextLong() % 100));
		}

		H2HWaiter waiter = new H2HWaiter(60);
		boolean allFinished;
		do {
			waiter.tickASecond();
			allFinished = true;

			for (TestProcessComponentListener listener : listeners) {
				allFinished &= listener.hasExecutionSucceeded();
			}
		} while (!allFinished);
	}

	/**
	 * Gets the user profile using the {@link UserProfileManager}.
	 * 
	 * @author Nico
	 */
	private class TestUserProfileStep extends ProcessStep<Void> {

		private final UserProfileManager profileManager;
		private final Operation operation;

		public TestUserProfileStep(UserProfileManager profileManager, Operation operation) {
			this.profileManager = profileManager;
			this.operation = operation;
		}

		@Override
		protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				if (operation == Operation.PUT) {
					profileManager.modifyUserProfile(getID(), new IUserProfileModification() {

						@Override
						public void modifyUserProfile(UserProfile userProfile) {
							if (operation == Operation.MODIFY) {
								new FolderIndex(userProfile.getRoot(), null, randomString());
							}
						}
					});
				} else {
					profileManager.readUserProfile();
				}
			} catch (GetFailedException | PutFailedException | AbortModifyException e) {
				throw new ProcessExecutionException(this, e);
			}
			return null;
		}
	}

	@Test
	public void testStress() throws NoSessionException, GetFailedException, PutFailedException, IOException,
			NoPeerConnectionException, AbortModifyException {
		File root = tempFolder.newFolder();
		UserProfileManager profileManager = new UserProfileManager(client.getDataManager(), userCredentials);
		for (int i = 0; i < 10; i++) {
			final boolean isFolder = rnd.nextBoolean();

			final File file = new File(root, randomString());
			if (!isFolder) {
				FileUtils.writeStringToFile(file, randomString());
			}
			final byte[] md5Hash = HashUtil.hash(file);
			final KeyPair fileKeys = generateRSAKeyPair(RSA_KEYLENGTH.BIT_512);

			while (true) {
				String pid = UUID.randomUUID().toString();
				profileManager.modifyUserProfile(pid, new IUserProfileModification() {

					@Override
					public void modifyUserProfile(UserProfile userProfile) {
						List<FolderIndex> indexes = getIndexList(userProfile.getRoot());
						if (isFolder) {
							new FolderIndex(indexes.get(rnd.nextInt(indexes.size())), fileKeys, randomString());
						} else {
							new FileIndex(indexes.get(rnd.nextInt(indexes.size())), fileKeys, file.getName(), md5Hash);
						}
					}
				});
				break;
			}
		}
	}

	public static List<FolderIndex> getIndexList(Index node) {
		List<FolderIndex> digest = new ArrayList<FolderIndex>();
		if (node.isFolder()) {
			// add self
			digest.add((FolderIndex) node);
			// add children
			for (Index child : ((FolderIndex) node).getChildren()) {
				if (child.isFolder())
					digest.addAll(getIndexList(child));
			}
		}
		return digest;
	}
}
