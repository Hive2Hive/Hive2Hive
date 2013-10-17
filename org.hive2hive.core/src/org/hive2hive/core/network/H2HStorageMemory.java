package org.hive2hive.core.network;

import java.security.PublicKey;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public class H2HStorageMemory extends StorageMemory {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HStorageMemory.class);

	public H2HStorageMemory(NetworkManager aNetworkManager) {
	}

	@Override
	public PutStatus put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data newData,
			PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		// TODO implement validation strategies
		return super.put(locationKey, domainKey, contentKey, newData, publicKey, putIfAbsent, domainProtection);
	}

}
