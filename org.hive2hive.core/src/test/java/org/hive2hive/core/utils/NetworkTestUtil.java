package org.hive2hive.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.helper.TestFileAgent;

// TODO test classes using a test network should inherit from the same test class that handles instantiation and shutdown of network, all in the same way

/**
 * Helper class for testing. Provides methods for creating, shutdown nodes and some random generators.
 * 
 * @author Seppi, Nico
 */
public class NetworkTestUtil {

	/**
	 * Creates a network with the given number of nodes. First node in the list is the
	 * initial node where all other nodes bootstrapped to him.</br>
	 * <b>Important:</b> After usage please shutdown the network. See {@link NetworkTestUtil#shutdownNetwork}
	 * 
	 * @param numberOfNodes
	 *            size of the network (has to be larger than one)
	 * @return list containing all nodes where the first one is the bootstrapping node (initial)
	 */
	public static ArrayList<NetworkManager> createNetwork(int numberOfNodes) {
		if (numberOfNodes < H2HConstants.REPLICATION_FACTOR) {
			throw new IllegalArgumentException(String.format("Network size must be at least %s (replication factor).",
					H2HConstants.REPLICATION_FACTOR));
		}

		ArrayList<NetworkManager> nodes = new ArrayList<NetworkManager>(numberOfNodes);

		// create the first node (initial)
		INetworkConfiguration netConfig = NetworkConfiguration.createInitialLocalPeer("Node A");
		NetworkManager initial = new NetworkManager(netConfig, new H2HDummyEncryption(), new EventBus());
		initial.connect();
		nodes.add(initial);

		// create the other nodes and bootstrap them to the initial peer
		char letter = 'A';
		for (int i = 1; i < numberOfNodes; i++) {
			INetworkConfiguration otherNetConfig = NetworkConfiguration.createLocalPeer(String.format("Node %s", ++letter),
					initial.getConnection().getPeerDHT().peer());
			NetworkManager node = new NetworkManager(otherNetConfig, new H2HDummyEncryption(), new EventBus());
			node.connect();
			nodes.add(node);
		}

		return nodes;
	}

	/**
	 * Shutdown a network.
	 * 
	 * @param network
	 *            list containing all nodes which has to be disconnected.
	 */
	public static void shutdownNetwork(ArrayList<NetworkManager> network) {
		if (!network.isEmpty()) {
			// shutdown of master peer is enough
			network.get(0).disconnect();
		}
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
			UserCredentials userCredentials = H2HJUnitTest.generateRandomCredentials();

			IFileConfiguration fileConfig = FileConfiguration.createDefault();

			UserProfileManager profileManager = new UserProfileManager(node.getDataManager(), userCredentials);
			PublicKeyManager keyManager = new PublicKeyManager(userCredentials.getUserId(), keyPair, node.getDataManager());
			DownloadManager downloadManager = new DownloadManager(node.getDataManager(), node.getMessageManager(),
					keyManager, fileConfig);
			VersionManager<Locations> locationsManager = new VersionManager<>(node.getDataManager(),
					userCredentials.getUserId(), H2HConstants.USER_LOCATIONS);

			SessionParameters params = new SessionParameters(new TestFileAgent(), fileConfig);
			params.setDownloadManager(downloadManager);
			params.setKeyManager(keyManager);
			params.setUserProfileManager(profileManager);
			params.setLocationsManager(locationsManager);

			node.setSession(new H2HSession(params));
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
		UserCredentials userCredentials = H2HJUnitTest.generateRandomCredentials();
		for (NetworkManager node : network) {
			IFileConfiguration fileConfig = FileConfiguration.createDefault();

			UserProfileManager profileManager = new UserProfileManager(node.getDataManager(), userCredentials);
			PublicKeyManager keyManager = new PublicKeyManager(userCredentials.getUserId(), keyPair, node.getDataManager());
			DownloadManager downloadManager = new DownloadManager(node.getDataManager(), node.getMessageManager(),
					keyManager, fileConfig);
			VersionManager<Locations> locationsManager = new VersionManager<>(node.getDataManager(),
					userCredentials.getUserId(), H2HConstants.USER_LOCATIONS);

			SessionParameters params = new SessionParameters(new TestFileAgent(), fileConfig);
			params.setDownloadManager(downloadManager);
			params.setKeyManager(keyManager);
			params.setUserProfileManager(profileManager);
			params.setLocationsManager(locationsManager);

			node.setSession(new H2HSession(params));
		}
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
		if (numberOfNodes < 1)
			throw new IllegalArgumentException("Invalid network size.");
		List<IH2HNode> nodes = new ArrayList<IH2HNode>(numberOfNodes);

		// create initial peer
		IH2HNode initial = H2HNode.createNode(NetworkConfiguration.createInitial("initial"),
				FileConfiguration.createDefault(), new H2HDummyEncryption());
		initial.connect();

		nodes.add(initial);

		try {
			InetAddress bootstrapAddress = InetAddress.getLocalHost();
			for (int i = 1; i < numberOfNodes; i++) {
				IH2HNode node = H2HNode.createNode(NetworkConfiguration.create("node " + i, bootstrapAddress),
						FileConfiguration.createDefault(), new H2HDummyEncryption());
				node.connect();
				nodes.add(node);
			}
		} catch (UnknownHostException e) {
			// should not happen
		}

		return nodes;
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
	 * Selects a random node of the given network
	 * 
	 * @param network a list of online peers
	 * @return a random node in the list
	 */
	public static NetworkManager getRandomNode(List<NetworkManager> network) {
		return network.get(new Random().nextInt(network.size()));
	}
}
