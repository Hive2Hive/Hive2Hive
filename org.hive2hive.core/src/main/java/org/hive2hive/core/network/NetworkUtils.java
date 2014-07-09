package org.hive2hive.core.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

public class NetworkUtils {

	private NetworkUtils() {
		// only static methods
	}

	/**
	 * checks if a specific port is available.
	 * 
	 * @param port
	 *            the port to check for availability
	 */
	public static boolean isPortAvailable(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}
	}

	/**
	 * Selects the {@link PeerAddress} peer addresses with the lowest node id.
	 * 
	 * @param list
	 *            a list of {@link PeerAddress} peer addresses
	 * @return
	 *         the peer address with the lowest node id
	 */
	public static PeerAddress choseFirstPeerAddress(List<PeerAddress> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		Collections.sort(list, new PeerAddressComperator());
		return list.get(0);
	}

	private static class PeerAddressComperator implements Comparator<PeerAddress> {
		@Override
		public int compare(PeerAddress o1, PeerAddress o2) {
			return o1.getPeerId().toString().compareTo(o2.getPeerId().toString());
		}
	}
}
