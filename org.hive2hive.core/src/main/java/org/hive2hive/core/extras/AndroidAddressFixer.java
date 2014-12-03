package org.hive2hive.core.extras;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.tomp2p.peers.PeerAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extra
public class AndroidAddressFixer {

	private static final Logger logger = LoggerFactory.getLogger(AndroidAddressFixer.class);

	/**
	 * Workaround of Android generating invalid InetAdresses
	 * FIXME remove asap
	 */
	public static PeerAddress fix(PeerAddress peerAddress) {
		try {
			return peerAddress.changeAddress(InetAddress.getByAddress(peerAddress.inetAddress().getAddress()));
		} catch (UnknownHostException e) {
			// ignoring probably leads to SIGSEGV error and JVM crash
			logger.warn("Could not fix the address {}", peerAddress, e);
			return peerAddress;
		}
	}
}
