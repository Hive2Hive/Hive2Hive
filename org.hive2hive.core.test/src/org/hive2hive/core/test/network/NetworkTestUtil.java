package org.hive2hive.core.test.network;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HNodeBuilder;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IH2HNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Helper class for testing. Provides methods for creating, shutdown nodes and some random generators.
 * 
 * @author Seppi
 */
public class NetworkTestUtil {

	/**
	 * Creates a single node which is master.
	 * 
	 * @return a node
	 */
	public static NetworkManager createSingleNode() {
		NetworkManager node = new NetworkManager("Node A");
		node.connect();
		return node;
	}

	/**
	 * Creates a network with the given number of nodes. First node in the list is the
	 * master node where all other nodes bootstrapped to him.</br>
	 * <b>Important:</b> After usage please shutdown the network. See {@link NetworkTestUtil#shutdownNetwork}
	 * 
	 * @param numberOfNodes
	 *            size of the network (has to be larger than one)
	 * @return list containing all nodes where the first one is the bootstrapping node (master)
	 */
	public static List<NetworkManager> createNetwork(int numberOfNodes) {
		if (numberOfNodes < 1)
			throw new IllegalArgumentException("invalid size of network");
		List<NetworkManager> nodes = new ArrayList<NetworkManager>(numberOfNodes);

		// create the first node (master)
		NetworkManager master = new NetworkManager("Node A");
		master.connect();
		nodes.add(master);

		// create the other nodes and bootstrap them to the master peer
		char letter = 'A';
		for (int i = 1; i < numberOfNodes; i++) {
			NetworkManager node = new NetworkManager(String.format("Node %s", ++letter));
			try {
				node.connect(InetAddress.getByName("127.0.0.1"));
			} catch (UnknownHostException e) {
				// should not happen
			}
			nodes.add(node);
		}

		return nodes;
	}

	/**
	 * Generate and assign public/private key pairs to the nodes.
	 * 
	 * @param network
	 *            list containing all nodes which have different key pairs
	 */
	public static void createKeyPairs(List<NetworkManager> network) {
		for (NetworkManager node : network) {
			KeyPair keyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
			node.setSession(new H2HSession(keyPair, null, null, null));
		}
	}

	/**
	 * Generate and assign a public/private key pair to all nodes.
	 * 
	 * @param network
	 *            list containing all nodes which need to have the same key pair
	 */
	public static void createSameKeyPair(List<NetworkManager> network) {
		KeyPair keyPair = EncryptionUtil.generateRSAKeyPair(H2HConstants.H2H_RSA_KEYLENGTH);
		H2HSession session = new H2HSession(keyPair, null, null, null);
		for (NetworkManager node : network) {
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
			node.disconnect();
		}
	}

	/**
	 * Creates a <code>Hive2Hive</code> network with the given number of nodes. First node in the list is the
	 * master node where all other nodes bootstrapped to him.</br>
	 * <b>Important:</b> After usage please shutdown the network. See {@link NetworkTestUtil#shutdownNetwork}
	 * 
	 * @param numberOfNodes
	 *            size of the network (has to be larger than one)
	 * @return list containing all Hive2Hive nodes where the first one is the bootstrapping node (master)
	 */
	public static List<IH2HNode> createH2HNetwork(int numberOfNodes) {
		if (numberOfNodes < 1)
			throw new IllegalArgumentException("invalid size of network");
		List<IH2HNode> nodes = new ArrayList<IH2HNode>(numberOfNodes);

		// create a master
		File tempDirectory = FileUtils.getTempDirectory();
		File root = new File(tempDirectory, randomString());
		IH2HNode master = H2HNodeBuilder.buildDefault().setMaster(true).setRootPath(root.getAbsolutePath());
		nodes.add(master);

		try {
			InetAddress bootstrapAddress = InetAddress.getByName("127.0.0.1");
			for (int i = 1; i < numberOfNodes; i++) {
				root = new File(tempDirectory, randomString());
				IH2HNode node = H2HNodeBuilder.buildDefault().setBootstrapAddress(bootstrapAddress).setRootPath(root.getAbsolutePath());
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

}
