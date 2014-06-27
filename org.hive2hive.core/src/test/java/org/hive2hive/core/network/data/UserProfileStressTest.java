package org.hive2hive.core.network.data;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Seppi
 */
public class UserProfileStressTest extends H2HJUnitTest {

	private static UserCredentials userCredentials;
	private static NetworkManager client;
	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserProfileStressTest.class;
		beforeClass();
		client = NetworkTestUtil.createNetwork(1).get(0);
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		root = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.register(userCredentials, client);
	}

	@Test
	@Ignore
	public void test() throws NoSessionException, GetFailedException, PutFailedException, IOException,
			NoPeerConnectionException {
		UserProfileManager profileManager = new UserProfileManager(client.getDataManager(), userCredentials);
		Random random = new Random();

		while (true) {
			boolean isFolder = random.nextBoolean();

			File file = null;
			KeyPair keys = null;
			byte[] md5Hash = null;
			if (!isFolder) {
				file = new File(root, NetworkTestUtil.randomString());
				FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());
				keys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
				md5Hash = HashUtil.hash(file);
			}

			String pid = UUID.randomUUID().toString();
			UserProfile profile = profileManager.getUserProfile(pid, true);

			List<FolderIndex> indexes = getIndexList(profile.getRoot());

			if (isFolder) {
				new FolderIndex(indexes.get(random.nextInt(indexes.size())), null, NetworkTestUtil.randomString());
			} else {
				new FileIndex(indexes.get(random.nextInt(indexes.size())), keys, file.getName(), md5Hash);
			}

			profileManager.readyToPut(profile, pid);
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

	@AfterClass
	public static void cleanAfterClass() {
		client.disconnect();
		afterClass();
	}

}
