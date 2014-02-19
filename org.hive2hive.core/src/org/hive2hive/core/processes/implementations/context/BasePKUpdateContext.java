package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.network.data.NetworkContent;

/**
 * Abstract context to update a protection key (used for sharing / unsharing)
 * 
 * @author Nico
 * 
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

	public abstract NetworkContent getContent();

	public abstract String getLocationKey();

	public abstract String getContentKey();
}
