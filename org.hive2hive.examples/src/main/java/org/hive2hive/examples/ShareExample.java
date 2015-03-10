package org.hive2hive.examples;

import java.io.File;
import java.net.InetAddress;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.security.UserCredentials;

/**
 * This example shows some how to share a folder between two users.
 * 
 * @author Nico
 *
 */
public class ShareExample {

	public static void main(String[] args) throws Exception {
		IFileConfiguration fileConfiguration = FileConfiguration.createDefault();

		// Create two nodes and open a new overlay network
		IH2HNode node1 = H2HNode.createNode(fileConfiguration);
		IH2HNode node2 = H2HNode.createNode(fileConfiguration);
		node1.connect(NetworkConfiguration.createInitial());
		node2.connect(NetworkConfiguration.create(InetAddress.getLocalHost()));

		// These two file agents are used to configure the root directory of the logged in users
		ExampleFileAgent node1FileAgent = new ExampleFileAgent();
		ExampleFileAgent node2FileAgent = new ExampleFileAgent();

		// Register and login user 'Alice' at node 1
		UserCredentials alice = new UserCredentials("Alice", "password", "pin");
		node1.getUserManager().createRegisterProcess(alice).execute();
		node1.getUserManager().createLoginProcess(alice, node1FileAgent).execute();

		// Register and login user 'Bob' at node 2
		UserCredentials bob = new UserCredentials("Bob", "password", "pin");
		node2.getUserManager().createRegisterProcess(bob).execute();
		node2.getUserManager().createLoginProcess(bob, node2FileAgent).execute();

		// Let's create a folder at Alice and upload it
		IFileManager fileManager1 = node1.getFileManager(); // The file management of Alice's peer
		File folderAtAlice = new File(node1FileAgent.getRoot(), "shared-folder");
		folderAtAlice.mkdirs();
		fileManager1.createAddProcess(folderAtAlice).execute();

		// Let's share the folder with Bob giving him write permission
		fileManager1.createShareProcess(folderAtAlice, "Bob", PermissionType.WRITE).execute();

		// Wait some time in order to get the file share event propagated to Bob
		System.out.println("Alice shared a folder with Bob. We'll wait some time for its propagation...");
		Thread.sleep(20000);

		// Bob can now 'download' the folder (yes, sounds a little bit silly...)
		IFileManager fileManager2 = node2.getFileManager(); // The file management of Bob's peer
		File folderAtBob = new File(node2FileAgent.getRoot(), "shared-folder");
		fileManager2.createDownloadProcess(folderAtBob).execute();

		// Bob could for example upload a new file to the shared folder
		File fileAtBob = new File(folderAtBob, "shared-file.txt");
		FileUtils.write(fileAtBob, "This is a shared file of Alice and Bob");
		fileManager2.createAddProcess(fileAtBob).execute();

		// Wait some time in order to get the file share event propagated to Bob
		System.out.println("Waiting that Alice sees the file from Bob...");
		Thread.sleep(20000);

		// Alice now can obtain the shared file
		File fileAtAlice = new File(folderAtAlice, "shared-file.txt");
		fileManager1.createDownloadProcess(fileAtAlice).execute();
		System.out.println("Content of the file in the shared folder at Alice: " + FileUtils.readFileToString(fileAtAlice));
	}
}
