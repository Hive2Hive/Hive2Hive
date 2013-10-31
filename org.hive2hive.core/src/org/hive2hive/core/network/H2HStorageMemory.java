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

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HStorageMemory.class);

	private final NetworkManager networkManager;

	public H2HStorageMemory(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public PutStatus put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data newData,
			PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		// TODO this method receives another Number160 parameter for the version

		// The version key (160bit) is split into two parts: The timestamp (64bit) and the hash of the
		// previous version (96bit). We can verify if the put is valid if the previous version is the latest
		// one (with the highest timestamp).

		// if the previous version is the latest one accept it (continue).
		// if the previous version is already outdated (or not existent), return PutStatus.VERSION_CONFLICT

		// After adding the content to the memory, old versions should be cleaned up. How many old versions we
		// keep could probably be parameterized. I (Nico) would recommend to keep at least 2 or 3 versions,
		// thus we can recognize concurrent modification better (else, the 'previous version' hash
		// is always wrong).

		// TODO implement the crap above

		return super.put(locationKey, domainKey, contentKey, newData, publicKey, putIfAbsent,
				domainProtection);
	}
}
