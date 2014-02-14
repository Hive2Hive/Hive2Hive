package org.hive2hive.core.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;

public class Client {

	public Client() throws UnknownHostException {

		// configs
		INetworkConfiguration networkConfig = NetworkConfiguration.create("nodeID",
				InetAddress.getLocalHost());
		IFileConfiguration fileConfig = FileConfiguration.createDefault();

		// node
		IH2HNode node = H2HNode.createNode(networkConfig, fileConfig);
		node.connect();

		// managers
		IUserManager userManager = node.getUserManager();
		IFileManager fileManager = node.getFileManager();

		// operations
		UserCredentials credentials = new UserCredentials("biocoder", "pw", "123456");

		try {
			node.getUserManager().register(credentials);
		} catch (NoPeerConnectionException e) {
			e.printStackTrace();
		}

		node.disconnect();
	}
}