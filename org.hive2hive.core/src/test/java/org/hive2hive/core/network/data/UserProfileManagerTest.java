package org.hive2hive.core.network.data;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.H2HWaiter;
import org.hive2hive.processframework.util.TestProcessComponentListener;
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

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;

	private UserCredentials userCredentials;
	private NetworkManager client;
	private File root;

	private enum Operation {
		PUT,
		GET,
		MODIFY
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	@Before
	public void setup() throws NoPeerConnectionException, InvalidCipherTextException, IOException {
		userCredentials = generateRandomCredentials();
		client = NetworkTestUtil.getRandomNode(network);
		root = FileTestUtil.getTempDirectory();

		// create an user profile
		UserProfile userProfile = new UserProfile(userCredentials.getUserId());
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

		H2HWaiter waiter = new H2HWaiter(60);
		boolean allFinished;
		do {
			waiter.tickASecond();
			allFinished = true;

			for (TestProcessComponentListener listener : listeners) {
				allFinished &= listener.hasSucceeded();
			}
		} while (!allFinished);
	}

	/**
	 * Gets the user profile using the {@link UserProfileManager}.
	 * 
	 * @author Nico
	 */
	private class TestUserProfileStep extends ProcessStep {

		private final UserProfileManager profileManager;
		private final Operation operation;

		public TestUserProfileStep(UserProfileManager profileManager, Operation operation) {
			this.profileManager = profileManager;
			this.operation = operation;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				UserProfile userProfile = profileManager.getUserProfile(getID(), operation == Operation.PUT);

				if (operation == Operation.MODIFY) {
					new FolderIndex(userProfile.getRoot(), null, randomString());
				}

				if (operation == Operation.PUT) {
					profileManager.readyToPut(userProfile, getID());
				}

			} catch (GetFailedException | PutFailedException e) {
				throw new ProcessExecutionException(e);
			}
		}
	}

	@Test
	public void testStress() throws NoSessionException, GetFailedException, PutFailedException, IOException,
			NoPeerConnectionException {
		UserProfileManager profileManager = new UserProfileManager(client.getDataManager(), userCredentials);
		Random random = new Random();

		for (int i = 0; i < 10; i++) {
			boolean isFolder = random.nextBoolean();

			File file = null;
			KeyPair keys = null;
			byte[] md5Hash = null;
			if (!isFolder) {
				file = new File(root, randomString());
				FileUtils.writeStringToFile(file, randomString());
				keys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
				md5Hash = HashUtil.hash(file);
			}

			while (true) {
				String pid = UUID.randomUUID().toString();
				UserProfile profile = profileManager.getUserProfile(pid, true);

				List<FolderIndex> indexes = getIndexList(profile.getRoot());

				if (isFolder) {
					new FolderIndex(indexes.get(random.nextInt(indexes.size())), null, randomString());
				} else {
					new FileIndex(indexes.get(random.nextInt(indexes.size())), keys, file.getName(), md5Hash);
				}

				try {
					profileManager.readyToPut(profile, pid);
				} catch (VersionForkAfterPutException e) {
					continue;
				}
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
