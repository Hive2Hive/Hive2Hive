package org.hive2hive.core.network;

import java.security.PublicKey;

import net.tomp2p.dht.StorageLayer;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to deny data. <b>Important:</b> This feature is used only for testing purposes.
 * 
 * @author Seppi, Nico
 */
public class H2HStorageMemory extends StorageLayer {

	private static final Logger logger = LoggerFactory.getLogger(H2HStorageMemory.class);

	public enum StorageMemoryMode {
		/** the normal behavior, where each 'put' is checked for version conflicts */
		STANDARD,

		/** Every request to store will fail and returns a {@link PutStatusH2H#FAILED} */
		DENY_ALL
	}

	private StorageMemoryMode mode;

	public H2HStorageMemory() {
		super(new StorageMemory());
		this.mode = StorageMemoryMode.STANDARD;
	}

	public void setMode(StorageMemoryMode mode) {
		assert mode != null;
		this.mode = mode;
	}

	@Override
	public Enum<?> put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		switch (mode) {
			case STANDARD: {
				return super.put(key, newData, publicKey, putIfAbsent, domainProtection);
			}
			case DENY_ALL: {
				logger.warn("Memory mode is denying the put request.");
				return PutStatus.FAILED;
			}
			default: {
				logger.error("Invalid mode {}. Returning a failure.", mode);
				return PutStatus.FAILED;
			}
		}
	}

}
