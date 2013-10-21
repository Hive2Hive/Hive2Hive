package org.hive2hive.core.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class NetworkUtils {

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

		return false;
	}

}
