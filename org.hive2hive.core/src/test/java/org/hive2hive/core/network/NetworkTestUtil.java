package org.hive2hive.core.network;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.UserCredentials;

// TODO NetworkTestUtil#createNetwork and NetwortTestUtil#createH2HNetwork seem to be redundant!! remove!
// TODO test classes using a test network should inherit from the same test class that handles instantiation and shutdown of network, all in the same way

/**
 * Helper class for testing. Provides methods for creating, shutdown nodes and some random generators.
 * 
 * @author Seppi
 */
public class NetworkTestUtil {

	/**
	 * Same as {@link NetworkTestUtil#createNetwork(int)} but with own encryption implementation (instead of
	 * standard one)
	 */
	public static List<NetworkManager> createNetwork(int numberOfNodes, IH2HEncryption encryption) {
		if (numberOfNodes < 1)
			throw new IllegalArgumentException("invalid size of network");
		List<NetworkManager> nodes = new ArrayList<NetworkManager>(numberOfNodes);

		// create the first node (initial)
		INetworkConfiguration netConfig = NetworkConfiguration.create("Node A");
		NetworkManager initial = new NetworkManager(netConfig, new H2HDummyEncryption());
		initial.connect();
		nodes.add(initial);

		// create the other nodes and bootstrap them to the initial peer
		char letter = 'A';
		for (int i = 1; i < numberOfNodes; i++) {
			INetworkConfiguration otherNetConfig = NetworkConfiguration.createLocalPeer(String.format("Node %s", ++letter),
					initial.getConnection().getPeer());
			NetworkManager node = new NetworkManager(otherNetConfig, encryption);
			node.connect();
			nodes.add(node);
		}

		return nodes;
	}

	/**
	 * Creates a network with the given number of nodes. First node in the list is the
	 * initial node where all other nodes bootstrapped to him.</br>
	 * <b>Important:</b> After usage please shutdown the network. See {@link NetworkTestUtil#shutdownNetwork}
	 * 
	 * @param numberOfNodes
	 *            size of the network (has to be larger than one)
	 * @return list containing all nodes where the first one is the bootstrapping node (initial)
	 */
	public static List<NetworkManager> createNetwork(int numberOfNodes) {
		return createNetwork(numberOfNodes, new H2HDummyEncryption());
	}

	/**
	 * Generate and assign public/private key pairs to the nodes.
	 * 
	 * @param network
	 *            list containing all nodes which have different key pairs
	 * @throws NoPeerConnectionException
	 */
	public static void setDifferentSessions(List<NetworkManager> network) throws NoPeerConnectionException {
		for (NetworkManager node : network) {
			KeyPair keyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
			UserCredentials userCredentials = generateRandomCredentials();
			UserProfileManager profileManager = new UserProfileManager(node.getDataManager(), userCredentials);
			PublicKeyManager keyManager = new PublicKeyManager(userCredentials.getUserId(), keyPair, node.getDataManager());
			IFileConfiguration config = FileConfiguration.createDefault();
			DownloadManager downloadManager = new DownloadManager(node.getDataManager(), node.getMessageManager(),
					keyManager, config);
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			H2HSession session;
			session = new H2HSession(profileManager, keyManager, downloadManager, config, root.toPath());
			node.setSession(session);
		}
	}

	/**
	 * Generate and assign a public/private key pair to all nodes.
	 * 
	 * @param network
	 *            list containing all nodes which need to have the same key pair
	 * @throws NoPeerConnectionException
	 */
	public static void setSameSession(List<NetworkManager> network) throws NoPeerConnectionException {
		KeyPair keyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
		UserCredentials userCredentials = generateRandomCredentials();
		for (NetworkManager node : network) {
			UserProfileManager profileManager = new UserProfileManager(node.getDataManager(), userCredentials);
			PublicKeyManager keyManager = new PublicKeyManager(userCredentials.getUserId(), keyPair, node.getDataManager());
			IFileConfiguration config = FileConfiguration.createDefault();
			DownloadManager downloadManager = new DownloadManager(node.getDataManager(), node.getMessageManager(),
					keyManager, config);
			File root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
			H2HSession session;
			session = new H2HSession(profileManager, keyManager, downloadManager, config, root.toPath());
			node.setSession(session);
		}
	}

	/**
	 * Shutdown a network.
	 * 
	 * @param network
	 *            list containing all nodes which has to be disconnected.
	 */
	public static void shutdownNetwork(List<NetworkManager> network) {
		for (NetworkManager node : network) {
			if (node.getConnection().isConnected())
				node.disconnect();
		}
	}

	/**
	 * Same as {@link NetworkTestUtil#createH2HNetwork(int)} but with own encryption implementation
	 */
	public static List<IH2HNode> createH2HNetwork(int numberOfNodes, IH2HEncryption encryption) {
		if (numberOfNodes < 1)
			throw new IllegalArgumentException("Invalid network size.");
		List<IH2HNode> nodes = new ArrayList<IH2HNode>(numberOfNodes);

		// create initial peer
		IH2HNode initial = H2HNode.createNode(NetworkConfiguration.create("initial"), FileConfiguration.createDefault(),
				encryption);
		initial.connect();
		initial.getFileManager().configureAutostart(false);
		initial.getUserManager().configureAutostart(false);

		nodes.add(initial);

		try {
			InetAddress bootstrapAddress = InetAddress.getLocalHost();
			for (int i = 1; i < numberOfNodes; i++) {
				IH2HNode node = H2HNode.createNode(NetworkConfiguration.create("node " + i, bootstrapAddress),
						FileConfiguration.createDefault(), encryption);
				node.connect();
				node.getFileManager().configureAutostart(false);
				node.getUserManager().configureAutostart(false);
				nodes.add(node);
			}
		} catch (UnknownHostException e) {
			// should not happen
		}

		return nodes;
	}

	/**
	 * Creates a <code>Hive2Hive</code> network with the given number of nodes. First node in the list is the
	 * initial node where all other nodes bootstrapped to him.</br>
	 * <b>Important:</b> After usage please shutdown the network. See {@link NetworkTestUtil#shutdownNetwork}
	 * 
	 * @param numberOfNodes
	 *            size of the network (has to be larger than one)
	 * @return list containing all Hive2Hive nodes where the first one is the bootstrapping node (initial)
	 */
	public static List<IH2HNode> createH2HNetwork(int numberOfNodes) {
		return createH2HNetwork(numberOfNodes, new H2HDummyEncryption());
	}

	/**
	 * Shutdown a network.
	 * 
	 * @param network
	 *            list containing all nodes which has to be disconnected.
	 */
	public static void shutdownH2HNetwork(List<IH2HNode> network) {
		for (IH2HNode node : network) {
			node.disconnect();
		}
	}

	/**
	 * Generates a random string (based on UUID)
	 * 
	 * @return a random string
	 */
	public static String randomString() {
		return UUID.randomUUID().toString();
	}

	public static UserCredentials generateRandomCredentials() {
		return new UserCredentials(NetworkTestUtil.randomString(), NetworkTestUtil.randomString(),
				NetworkTestUtil.randomString());
	}

	public static NetworkManager getRandomNode(List<NetworkManager> network) {
		return network.get(new Random().nextInt(network.size()));
	}
}
