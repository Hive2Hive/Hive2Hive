package org.hive2hive.core.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;

public class Client {

	public Client() throws UnknownHostException {

		INetworkConfiguration networkConfig = NetworkConfiguration.createMasterNetworkConfiguration("nodeID",
				InetAddress.getLocalHost());
		IFileConfiguration fileConfig = FileConfiguration.createDefaultFileConfiguration();

		ProcessManager processManager = new ProcessManager(true);

		IUserManager userManager = new UserManager(networkConfig, processManager);
//		userManager.connect();

		IFileManager fileManager = new FileManager(networkConfig, fileConfig, processManager);
//		fileManager.connect();

//		IH2HNode node = new H2HNode.H2HNodeBuilder(networkConfig).setUserManager(userManager)
//				.setFileManager(fileManager).build();
//		node.connect();
//		node.disconnect();
		
	}
}
