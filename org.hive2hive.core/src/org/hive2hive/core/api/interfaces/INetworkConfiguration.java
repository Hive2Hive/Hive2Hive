package org.hive2hive.core.api.interfaces;

import java.net.InetAddress;

import org.hive2hive.core.H2HConstants;

/**
 * Configuration of the network settings for a Hive2Hive node.
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface INetworkConfiguration {

	/**
	 * Each node has a unique ID corresponding to the ID in the DHT.
	 * 
	 * @return the ID of this node.
	 */
	String getNodeID();

	/**
	 * Returns whether this peer is master. When a peer is master, he's the first one in the network and does
	 * not try to bootstrap anywhere. In each network, only one master peer needs to exist. When other peers
	 * have joined, the master peer can also go offline.
	 * 
	 * @return true when this peer is master.
	 */
	boolean isMasterPeer();

	/**
	 * If this peer is not master, it needs to boostrap anywhere to connect to the p2p network.
	 * 
	 * @return the internet address to boostrap to. Make sure the given address is reachable (firewalls).
	 */
	InetAddress getBootstrapAddress();

	/**
	 * The port to bootstrap to. This depends on the configuration of the network and possibly of
	 * port-forwarding. The default port of Hive2Hive is {@link H2HConstants#H2H_PORT}. This port is
	 * automatically increased in case it's already used; thus check with your peer to bootstrap to.
	 * 
	 * @return the port of the peer this node bootstraps to.
	 */
	int getBootstrapPort();
}
