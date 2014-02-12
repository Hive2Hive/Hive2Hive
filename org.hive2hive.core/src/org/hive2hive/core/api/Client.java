package org.hive2hive.core.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;

public class Client {

	public Client() throws UnknownHostException {

		INetworkConfiguration networkConfig = NetworkConfiguration.create("nodeID",
				InetAddress.getLocalHost());
		IFileConfiguration fileConfig = FileConfiguration.createDefault();

		ProcessManager processManager = new ProcessManager(true);

//		INetworkNode node = new H2HNode(networkConfig);
//
//		IUserManager userManager = new H2HUserManager();
//		node.attach(userManager);
//
//		IFileManager fileManager = new H2HFileManager(fileConfig);
		
		
		

		// IH2HNode node = new H2HNode.H2HNodeBuilder(networkConfig).setUserManager(userManager)
		// .setFileManager(fileManager).build();
		// node.connect();
		// node.disconnect();

	}
}
