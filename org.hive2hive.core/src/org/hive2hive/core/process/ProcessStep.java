package org.hive2hive.core.process;

import java.security.PublicKey;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * This class represents a single step of a {@link Process}. This step calls the next step after being
 * finished.
 * 
 * @author Nico
 * 
 */
public abstract class ProcessStep {

	private Process process;

	/**
	 * Starts the execution of this process step.
	 */
	public abstract void start();

	/**
	 * Tells this step to undo any work it did previously. If this step changed anything in the network it
	 * needs to be revoked completely. After the execution of this method, the global state of the network
	 * needs to be the same as if this step never existed.
	 */
	public abstract void rollBack();

	public void setProcess(Process process) {
		this.process = process;
	}

	protected Process getProcess() {
		return process;
	}

	protected NetworkManager getNetworkManager() {
		return process.getNetworkManager();
	}

	public static String key2String(PublicKey key) {
		return EncryptionUtil.byteToHex(key.getEncoded());
	}
}
