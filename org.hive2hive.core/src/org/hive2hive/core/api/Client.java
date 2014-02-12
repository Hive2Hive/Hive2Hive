package org.hive2hive.core.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.api.configs.INetworkConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;

public class Client {

	public Client() throws UnknownHostException {

		// configs
		INetworkConfiguration networkConfig = NetworkConfiguration.create("nodeID",
				InetAddress.getLocalHost());
		IFileConfiguration fileConfig = FileConfiguration.createDefault();

		// components
		H2HUserManager userManager = new H2HUserManager();
		userManager.configureAutostart(true);

		H2HFileManager fileManager = new H2HFileManager(fileConfig);
		fileManager.configureAutostart(false);
		
		// node
		H2HNode node = new H2HNode(networkConfig);
		node.attach(userManager);
		node.attach(fileManager);
		node.connect();
		
		// operations
		UserCredentials credentials = new UserCredentials("biocoder", "pw", "123456");
		
		// TODO avoid multi-try-catch blocks, e.g. throw only a H2HException wrapper
		try {
			userManager.register(credentials);
		} catch (NoNetworkException e) {
			e.printStackTrace();
		} catch (NoPeerConnectionException e) {
			e.printStackTrace();
		}
	
	}
}