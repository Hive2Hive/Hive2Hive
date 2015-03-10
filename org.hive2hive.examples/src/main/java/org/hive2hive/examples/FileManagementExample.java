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
import org.hive2hive.core.security.UserCredentials;

/**
 * This example shows some file synchronization operations a user can perform
 * 
 * @author Nico
 *
 */
public class FileManagementExample {

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

		// Also login user 'Alice' at node 2
		node2.getUserManager().createLoginProcess(alice, node2FileAgent).execute();

		// All file management operations are located at the file manager. Here we get the file managers of
		// each peer alice is connected to.
		IFileManager fileManager1 = node1.getFileManager(); // for node 1
		IFileManager fileManager2 = node2.getFileManager(); // for node 2

		// Let's create a file and upload it at node 1
		File fileAtNode1 = new File(node1FileAgent.getRoot(), "test-file.txt");
		FileUtils.write(fileAtNode1, "Hello"); // add some content
		fileManager1.createAddProcess(fileAtNode1).execute();

		// Normally, the node 2 would be notified about the new file through the event bus (shown in another
		// example). However, we just know that the file exists in the network and can download it at node 2.
		// This is only possible because Alice is logged in into node 2 as well.
		File fileAtNode2 = new File(node2FileAgent.getRoot(), "test-file.txt");
		// this file does not exist yet (as we did not start the download process yet)
		System.out.println("Existence of the file prior to download: " + fileAtNode2.exists());
		fileManager2.createDownloadProcess(fileAtNode2).execute();

		// We can now re-check whether the file exists or not
		System.out.println("Existence of the file after download: " + fileAtNode2.exists());
		System.out.println("Content of the first version at node 2: " + FileUtils.readFileToString(fileAtNode2));

		// Now, let's modify the file at node 2 and re-upload a new version of it
		FileUtils.write(fileAtNode2, " World!", true); // append the text
		fileManager2.createUpdateProcess(fileAtNode2).execute();

		// The file has now updated, therefore we should download the new version at node 1
		fileManager1.createDownloadProcess(fileAtNode1).execute();
		System.out.println("Content of the second version at node 1: " + FileUtils.readFileToString(fileAtNode1));
	}
}
