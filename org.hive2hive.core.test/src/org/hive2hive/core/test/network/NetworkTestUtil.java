package org.hive2hive.core.test.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hive2hive.core.network.NetworkManager;

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
	 * Creates a <code>hive2hive</code> network with the given number of nodes. First node in the list is the
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
		List<NetworkManager> nodes = new ArrayList<NetworkManager>();

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
	
	public static String randomString(){
		return UUID.randomUUID().toString();
	}
}
