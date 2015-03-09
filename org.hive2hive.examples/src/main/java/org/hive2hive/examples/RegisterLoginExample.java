package org.hive2hive.examples;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * This example shows how a user can register and login
 * 
 * @author Nico
 *
 */
public class RegisterLoginExample {

	public static void main(String[] args) throws NoPeerConnectionException, InvalidProcessStateException,
			ProcessExecutionException {
		// Create a node and open a new overlay network
		IH2HNode node = H2HNode.createNode(FileConfiguration.createDefault());
		node.connect(NetworkConfiguration.createInitial());

		// The register functionality is in the user manager API
		IUserManager userManager = node.getUserManager();

		// Create user credentials to register a new user 'Alice'
		UserCredentials alice = new UserCredentials("Alice", "very-secret-password", "secret-pin");

		// Create a new register process and start it (blocking)
		IProcessComponent<Void> registerAlice = userManager.createRegisterProcess(alice);
		registerAlice.execute();

		// Check if Alice is now registered
		boolean aliceRegistered = userManager.isRegistered("Alice");
		System.out.println("Alice is registered: " + aliceRegistered);

		// Let's login to Alice's user account (blocking)
		IProcessComponent<Void> loginAlice = userManager.createLoginProcess(alice, new ExampleFileAgent());
		loginAlice.execute();

		// Check if Alice is now logged in
		System.out.println("Alice is logged in: " + userManager.isLoggedIn());
	}
}
