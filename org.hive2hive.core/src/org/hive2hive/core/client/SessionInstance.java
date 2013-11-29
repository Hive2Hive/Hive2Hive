package org.hive2hive.core.client;

import java.util.ArrayList;

import org.hive2hive.core.H2HNode;
import org.hive2hive.core.H2HNodeBuilder;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;

/**
 * A data class for the {@link ConsoleClient} that is necessary to store the client's session data.
 * 
 * @author Christian
 * 
 */
public final class SessionInstance {

	// network configuration parameters
	private ArrayList<NetworkManager> network = null;
	private final H2HNodeBuilder nodeBuilder = new H2HNodeBuilder();
	private H2HNode node;

	// user configuration parameters
	private String userId = null;
	private String password = null;
	private String pin = null;
	private UserCredentials credentials;

	public void setH2HNode(H2HNode node) {
		this.node = node;
	}

	public H2HNode getH2HNode() {
		return node;
	}

	public ArrayList<NetworkManager> getNetwork() {
		return network;
	}

	public void setNetwork(ArrayList<NetworkManager> network) {
		this.network = network;
	}

	public H2HNodeBuilder getNodeBuilder() {
		return nodeBuilder;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public UserCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(UserCredentials credentials) {
		this.credentials = credentials;
	}

}
