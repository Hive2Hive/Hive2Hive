package org.hive2hive.core.processes.context;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;

/**
 * Abstract context to update a protection key (used for sharing / unsharing)
 * 
 * @author Nico
 */
public abstract class BasePKUpdateContext {

	private final KeyPair oldProtectionKeys;
	private final KeyPair newProtectionKeys;

	public BasePKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys) {
		this.oldProtectionKeys = oldProtectionKeys;
		this.newProtectionKeys = newProtectionKeys;
	}

	public KeyPair consumeNewProtectionKeys() {
		return newProtectionKeys;
	}

	public KeyPair consumeOldProtectionKeys() {
		return oldProtectionKeys;
	}

	public abstract String getLocationKey();

	public abstract String getContentKey();
	
	public abstract Number160 getVersionKey();

	public abstract int getTTL();
	
	public abstract byte[] getHash();
	
}
