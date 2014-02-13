package org.hive2hive.core.api;

import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.INetworkComponent;
import org.hive2hive.core.network.NetworkManager;

public class H2HNode {

	// TODO atm, this class is just a wrapper for the NetworkManager
	private final NetworkManager networkManager;
	
	public H2HNode(INetworkConfiguration networkConfiguration) {
		networkManager = new NetworkManager(networkConfiguration);
	}
	
	public void attach(INetworkComponent component) {
		component.setNetworkManager(networkManager);
	}
	
	public void detach(INetworkComponent component) {
		component.setNetworkManager(null);
	}

	public void connect() {
		networkManager.connect();
	}

	public void disconnect() {
		networkManager.disconnect();
	}
	
//	public IH2HNodeStatus getStatus() {
//		boolean connected = networkManager.getConnection().isConnected();
//		int numberOfProcs = ProcessManager.getInstance().getAllProcesses().size();
//		try {
//			H2HSession session = networkManager.getSession();
//			Path root = session.getFileManager().getRoot();
//			String userId = session.getCredentials().getUserId();
//			return new H2HNodeStatus(root.toFile(), userId, connected, numberOfProcs);
//		} catch (NoSessionException e) {
//			return new H2HNodeStatus(null, null, connected, numberOfProcs);
//		}
//	}

}