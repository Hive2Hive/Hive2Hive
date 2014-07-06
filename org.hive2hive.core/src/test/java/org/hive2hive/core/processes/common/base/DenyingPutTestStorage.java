package org.hive2hive.core.processes.common.base;

import java.security.PublicKey;

import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.network.H2HStorageMemory;

/**
 * A simple storage which denies all puts with a {@link PutStatusH2H#FAILED} failure.
 * 
 * @author Seppi
 */
public class DenyingPutTestStorage extends H2HStorageMemory {

	public DenyingPutTestStorage() {
		super();
	}

	@Override
	public PutStatusH2H put(Number640 key, Data value, PublicKey publicKey, boolean putIfAbsent,
			boolean domainProtection) {
		// doesn't accept any data
		return PutStatusH2H.FAILED;
	}
}