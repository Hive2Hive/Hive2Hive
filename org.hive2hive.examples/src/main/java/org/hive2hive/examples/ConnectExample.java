package org.hive2hive.examples;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;

/**
 * This example shows how a peer can connect to the overlay network.
 * 
 * @author Nico
 *
 */
public class ConnectExample {

	public static void main(String[] args) throws UnknownHostException {
		// Create a consistent file configuration for all nodes
		IFileConfiguration fileConfiguration = FileConfiguration.createDefault();

		// Initialize several nodes (not connected yet)
		IH2HNode node1 = H2HNode.createNode(fileConfiguration);
		IH2HNode node2 = H2HNode.createNode(fileConfiguration);
		IH2HNode node3 = H2HNode.createNode(fileConfiguration);
		IH2HNode node4 = H2HNode.createNode(fileConfiguration);

		// Create a new P2P network at the first (initial) peer
		node1.connect(NetworkConfiguration.createInitial());

		// Connect the 2nd peer to the network. This peer bootstraps to node1 running at the local host
		NetworkConfiguration node2Conf = NetworkConfiguration.create(InetAddress.getLocalHost());
		node2.connect(node2Conf);

		// The network configuration builder allows you to configure more details
		// here we set a custom (non-random) node id and a custom port that the node 3 binds to
		NetworkConfiguration node3Conf = NetworkConfiguration.create(InetAddress.getLocalHost()).setPort(4777)
				.setNodeId("node3");
		node3.connect(node3Conf);

		// Nodes can bootstrap to any of the connected peers. Therefore, we set that node4 should connect to
		// node3 to become a part of the P2P network
		NetworkConfiguration node4Conf = NetworkConfiguration.create(InetAddress.getLocalHost()).setBootstrapPort(4777);
		node4.connect(node4Conf);

		// We can test the connection status of these nodes
		System.out.println("Node 1 is connected: " + node1.isConnected());
		System.out.println("Node 2 is connected: " + node2.isConnected());
		System.out.println("Node 3 is connected: " + node3.isConnected());
		System.out.println("Node 4 is connected: " + node4.isConnected());
	}
}
