package org.hive2hive.examples;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.security.UserCredentials;

/**
 * This example shows how to listen to events and take respective actions.
 * 
 * @author Nico
 *
 */
public class EventsExample {

	public static void main(String[] args) throws Exception {
		IFileConfiguration fileConfiguration = FileConfiguration.createDefault();

		// Create two nodes and open a new overlay network
		IH2HNode node1 = H2HNode.createNode(fileConfiguration);
		IH2HNode node2 = H2HNode.createNode(fileConfiguration);
		node1.connect(NetworkConfiguration.createInitial());
		node2.connect(NetworkConfiguration.create(InetAddress.getLocalHost()));

		// These two file agents are used to configure the root directory of the logged in user
		ExampleFileAgent node1FileAgent = new ExampleFileAgent();
		ExampleFileAgent node2FileAgent = new ExampleFileAgent();

		// Register user 'Alice' and login her at node 1 and 2
		UserCredentials alice = new UserCredentials("Alice", "password", "pin");
		node1.getUserManager().createRegisterProcess(alice).execute();
		node1.getUserManager().createLoginProcess(alice, node1FileAgent).execute();
		node2.getUserManager().createLoginProcess(alice, node2FileAgent).execute();

		// In this example, a file event listener is registered at node 2. Therefore, we will listen to events
		// that happen by actions taken by node 1.
		node2.getFileManager().subscribeFileEvents(new ExampleEventListener(node2.getFileManager()));

		// To demonstrate the 'add' event, we will add a new file with node 1
		// Let's create a file and upload it at node 1
		File fileAtNode1 = new File(node1FileAgent.getRoot(), "test-file-event.txt");
		FileUtils.write(fileAtNode1, "Hello"); // add some content
		node1.getFileManager().createAddProcess(fileAtNode1).execute();

		// Let's trigger a deletion event
		fileAtNode1.delete();
		node1.getFileManager().createDeleteProcess(fileAtNode1).execute();
	}

	// A Strong reference is necessary if this object is not held in any variable, otherwise GC would clean it
	// and events are not triggered anymore. So keep either a reference to this listener object or add the
	// strong reference annotation.
	@Listener(references = References.Strong)
	private static class ExampleEventListener implements IFileEventListener {

		private final IFileManager fileManager;

		public ExampleEventListener(IFileManager fileManager) {
			this.fileManager = fileManager;
		}

		@Override
		@Handler
		public void onFileAdd(IFileAddEvent fileEvent) {
			System.out.println("File was added: " + fileEvent.getFile().getName());
			try {
				// download the new file
				fileManager.createDownloadProcess(fileEvent.getFile()).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		@Handler
		public void onFileUpdate(IFileUpdateEvent fileEvent) {
			System.out.println("File was updated: " + fileEvent.getFile().getName());
			try {
				// download the newest version
				fileManager.createDownloadProcess(fileEvent.getFile()).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		@Handler
		public void onFileDelete(IFileDeleteEvent fileEvent) {
			System.out.println("File was deleted: " + fileEvent.getFile().getName());
			// delete it at the event receiver as well
			fileEvent.getFile().delete();
		}

		@Override
		@Handler
		public void onFileMove(IFileMoveEvent fileEvent) {
			try {
				// Move the file to the new destination if it exists
				if (fileEvent.isFile() && fileEvent.getSrcFile().exists()) {
					FileUtils.moveFile(fileEvent.getSrcFile(), fileEvent.getDstFile());
					System.out.println("File was moved from " + fileEvent.getSrcFile() + " to " + fileEvent.getDstFile());
				} else if (fileEvent.isFolder() && fileEvent.getSrcFile().exists()) {
					FileUtils.moveDirectory(fileEvent.getSrcFile(), fileEvent.getDstFile());
					System.out.println("Folder was moved from " + fileEvent.getSrcFile() + " to " + fileEvent.getDstFile());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		@Handler
		public void onFileShare(IFileShareEvent fileEvent) {
			System.out.println("File was shared by " + fileEvent.getInvitedBy());
			// Currently, no further actions necessary. The invitation is accepted
			// automatically and 'onFileAdd' is called in an instant.
		}

	}
}
