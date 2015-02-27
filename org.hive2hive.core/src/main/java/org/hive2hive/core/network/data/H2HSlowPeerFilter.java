package org.hive2hive.core.network.data;

import net.tomp2p.p2p.SlowPeerFilter;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SlowPeerFilter} that can be set inactive and logs if requests are rejected.
 * 
 * @author Nico
 *
 */
public class H2HSlowPeerFilter extends SlowPeerFilter {

	private static final Logger logger = LoggerFactory.getLogger(H2HSlowPeerFilter.class);

	private final boolean active = !H2HConstants.STORE_DATA_SLOW_PEERS;

	@Override
	public boolean rejectDirectHit(PeerAddress peerAddress) {
		boolean reject = super.rejectDirectHit(peerAddress) && active;
		if (reject && logger.isTraceEnabled()) {
			logger.trace("Rejecting direct hit {}", peerAddress);
		}
		return reject;
	}

	@Override
	public boolean rejectPotentialHit(PeerAddress peerAddress) {
		boolean reject = super.rejectPotentialHit(peerAddress) && active;
		if (reject && logger.isTraceEnabled()) {
			logger.trace("Rejecting potential hit {}", peerAddress);
		}
		return reject;
	}
}
