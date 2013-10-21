package org.hive2hive.core.network;

import java.security.PublicKey;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * Every <code>Hive2Hive</code> node has validation strategies when data is
 * stored. This is realized by a customized storage memory. Before some data
 * gets stored on a node the put method gets called where the node can verify
 * the store request.
 * 
 * @author Seppi
 */
public class H2HStorageMemory extends StorageMemory {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(H2HStorageMemory.class);
	
	private final NetworkManager networkManager;

	public H2HStorageMemory(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public PutStatus put(Number160 locationKey, Number160 domainKey,
			Number160 contentKey, Data newData, PublicKey publicKey,
			boolean putIfAbsent, boolean domainProtection) {
		// TODO implement validation strategies
		return super.put(locationKey, domainKey, contentKey, newData,
				publicKey, putIfAbsent, domainProtection);
	}

}
