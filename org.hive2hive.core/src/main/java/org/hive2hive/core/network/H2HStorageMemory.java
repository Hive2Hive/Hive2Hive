package org.hive2hive.core.network;

import java.security.PublicKey;
import java.util.Map;

import net.tomp2p.dht.StorageLayer;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.peers.Number640;
import net.tomp2p.rpc.DigestInfo;
import net.tomp2p.storage.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to deny data and/or return manipulated data. <b>Important:</b> This features are used only for
 * testing purposes.
 * 
 * @author Seppi, Nico
 */
public class H2HStorageMemory extends StorageLayer {

	private static final Logger logger = LoggerFactory.getLogger(H2HStorageMemory.class);

	public enum StorageMemoryPutMode {
		/** the normal behavior, where each 'put' is checked for version conflicts */
		STANDARD,

		/** Every request to store will fail and returns a {@link net.tomp2p.dht.StorageLayer.PutStatus#FAILED} */
		DENY_ALL
	}

	public enum StorageMemoryGetMode {
		/** the get normal behavior */
		STANDARD,

		/** returns a given value */
		MANIPULATED
	}

	private Map<Number640, Data> manipulatedMap;

	private StorageMemoryPutMode putMode;
	private StorageMemoryGetMode getMode;

	public H2HStorageMemory() {
		super(new StorageMemory());
		this.putMode = StorageMemoryPutMode.STANDARD;
		this.getMode = StorageMemoryGetMode.STANDARD;
	}

	public void setPutMode(StorageMemoryPutMode mode) {
		assert mode != null;
		this.putMode = mode;
	}

	public void setGetMode(StorageMemoryGetMode mode) {
		assert mode != null;
		this.getMode = mode;
	}

	public void setManipulatedMap(Map<Number640, Data> manipulatedMap) {
		this.manipulatedMap = manipulatedMap;
	}

	@Override
	public Enum<?> put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		switch (putMode) {
			case STANDARD: {
				return super.put(key, newData, publicKey, putIfAbsent, domainProtection);
			}
			case DENY_ALL: {
				// logger.warn("Memory mode is denying the put request.");
				return PutStatus.FAILED;
			}
			default: {
				logger.error("Invalid mode {}. Returning a failure.", putMode);
				return PutStatus.FAILED;
			}
		}
	}

	public Map<Number640, Data> getLatestVersion(Number640 key) {
		switch (getMode) {
			case STANDARD: {
				return super.getLatestVersion(key);
			}
			case MANIPULATED: {
				return manipulatedMap;
			}
			default: {
				logger.error("Invalid mode {}. Returning null.", getMode);
				return null;
			}
		}
	}

	@Override
	public DigestInfo digest(Number640 from, Number640 to, int limit, boolean ascending) {
		switch (getMode) {
			case STANDARD: {
				return super.digest(from, to, limit, ascending);
			}
			case MANIPULATED: {
				DigestInfo digestInfo = new DigestInfo();
				for (Map.Entry<Number640, Data> entry : manipulatedMap.entrySet()) {
					digestInfo.put(entry.getKey(), entry.getValue().basedOnSet());
				}
				return digestInfo;
			}
			default: {
				logger.error("Invalid mode {}. Returning null.", getMode);
				return null;
			}
		}
	}

}
