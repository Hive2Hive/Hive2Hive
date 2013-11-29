package org.hive2hive.core.client.menu;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.hive2hive.core.client.ConsoleClient;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.network.NetworkManager;

/**
 * The network configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian
 * 
 */
public final class NetworkMenu extends ConsoleMenu {

	public H2HConsoleMenuItem SetBootStrapMenuItem;
	public H2HConsoleMenuItem CreateNetworkMenuItem;
	public H2HConsoleMenuItem CreateH2HNodeMenutItem;
	
	public NetworkMenu(Console console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void createItems() {
		SetBootStrapMenuItem = new H2HConsoleMenuItem("Set BootsrapAddress") {
			protected void execute() throws UnknownHostException {
				System.out.println("Specify BootstrapAddress:\n");
				InetAddress bootstrapAddress = InetAddress.getByName(awaitStringParameter());
				session.getNodeBuilder().setBootstrapAddress(bootstrapAddress);
				session.setBootstrapAddress(bootstrapAddress);
				printSuccess();
			}
		};
		CreateNetworkMenuItem = new H2HConsoleMenuItem("Create Network") {
			protected boolean preconditionsSatisfied() {
				// check if bootstrap address is set
				if (session.getBootstrapAddress() == null){
					printPreconditionError("Cannot create network: Please specify a bootstrap address first.");
					SetBootStrapMenuItem.invoke();
				}
				return true;
			};
			protected void execute() {
				createNetworkHandler();
				printSuccess();
			}
		};
		CreateH2HNodeMenutItem = new H2HConsoleMenuItem("Create H2H Node") {
			protected boolean preconditionsSatisfied() {
				if (session.getNetwork() == null) {
					printPreconditionError("Cannot create H2H Node: Please create a network first.");
					new NetworkMenu(console, session).CreateNetworkMenuItem.invoke();
				}
				return true;
			}

			protected void execute() {
				session.setH2HNode(session.getNodeBuilder().build());
				printSuccess();
			}
		};
	}
	
	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Set MaxFileSize") {
			protected void execute() {
				System.out.println("Specify MaxFileSize:\n");
				session.getNodeBuilder().setMaxFileSize(awaitIntParameter());
				printSuccess();
			}
		});
		add(new H2HConsoleMenuItem("Set MaxNumOfVersions") {
			protected void execute() {
				System.out.println("Specify MaxNumOfVersions:\n");
				session.getNodeBuilder().setMaxNumOfVersions(awaitIntParameter());
				printSuccess();
			}
		});
		add(new H2HConsoleMenuItem("Set MaxSizeAllVersions") {
			protected void execute() {
				System.out.println("Specify MaxSizeAllVersions:\n");
				session.getNodeBuilder().setMaxSizeAllVersions(awaitIntParameter());
				printSuccess();
			}
		});
		add(new H2HConsoleMenuItem("Set ChunkSize") {
			protected void execute() {
				System.out.println("Specify ChunkSize:\n");
				session.getNodeBuilder().setChunkSize(awaitIntParameter());
				printSuccess();
			}
		});
		add(new H2HConsoleMenuItem("Set AutostartProcesses") {
			protected void execute() {
				System.out.println("Specify AutostartProcesses:\n");
				session.getNodeBuilder().setAutostartProcesses(awaitBooleanParameter());
				printSuccess();
			}
		});
		add(new H2HConsoleMenuItem("Set IsMasterPeer") {
			protected void execute() {
				System.out.println("Specify IsMasterPeer:\n");
				session.getNodeBuilder().setIsMaster(awaitBooleanParameter());
				printSuccess();
			}
		});
		add(SetBootStrapMenuItem);
		add(new H2HConsoleMenuItem("Set RootPath") {
			protected void execute() {
				System.out.println("Specify RootPath:\n");
				session.getNodeBuilder().setRootPath(awaitStringParameter());
				printSuccess();
			}
		});
		add(CreateNetworkMenuItem);
		add(CreateH2HNodeMenutItem);
	}
	
	private void createNetworkHandler() {

		// TODO this whole procedure should exist as separate process

		// specify number of nodes
		System.out.println("Specify number of nodes:\n");
		int nrOfNodes = awaitIntParameter();
		if (nrOfNodes < 1) {
			throw new IllegalArgumentException("Invalid number of nodes.");
		}
		ArrayList<NetworkManager> nodes = new ArrayList<NetworkManager>(nrOfNodes);

		// create the first node (master)
		NetworkManager master = new NetworkManager("master");
		master.connect();
		nodes.add(master);

		// create the other nodes and bootstrap them to the master peer
		char letter = 'A';
		for (int i = 1; i < nrOfNodes; i++) {
			NetworkManager node = new NetworkManager(String.format("node %s", ++letter));
			try {
				// TODO check whether this is correct
				node.connect(InetAddress.getByName("127.0.0.1"));
			} catch (UnknownHostException e) {
			}
			nodes.add(node);
		}

		// store the nodes in the session instance
		session.setNetwork(nodes);

		// TODO wait here until the operation has completed
	}

	@Override
	public String getInstruction() {
		return "Please select a network configuration option:\n";
	}
}
