package org.hive2hive.core;

import java.io.File;

/**
 * @author Nico
 */
public class H2HNodeStatus implements IH2HNodeStatus {

	private final File root;
	private final String userId;
	private final boolean connected;
	private final int numberOfProcesses;

	// TODO needed anymore?
	public H2HNodeStatus(File root, String userId, boolean connected, int numberOfProcesses) {
		this.root = root;
		this.userId = userId;
		this.connected = connected;
		this.numberOfProcesses = numberOfProcesses;
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isLoggedIn() {
		return userId != null;
	}

	@Override
	public int getNumberOfProcesses() {
		return numberOfProcesses;
	}
}
