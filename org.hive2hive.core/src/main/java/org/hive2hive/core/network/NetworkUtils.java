package org.hive2hive.core.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {

	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	private static final int MAX_PORT = 65535;

	private NetworkUtils() {
		// only static methods
	}

	/**
	 * Searches for open ports, starting at {@link H2HConstants#H2H_PORT}.
	 * 
	 * @return the free port or -1 if none was found.
	 */
	public static int searchFreePort() {
		int port = H2HConstants.H2H_PORT;
		while (!isPortAvailable(port)) {
			if (port > MAX_PORT) {
				logger.error("Could not find any free port");
				return -1;
			}

			port++;
		}
		logger.debug("Found free port {}.", port);
		return port;
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
			return o1.peerId().toString().compareTo(o2.peerId().toString());
		}
	}
}
