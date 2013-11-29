package org.hive2hive.core.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.hive2hive.core.network.NetworkManager;

public class NetworkMenu extends ConsoleMenu {

	public NetworkMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuHandlers() {

		add("Set MaxFileSize", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set MaxFileSize");
				System.out.println("Specify MaxFileSize:\n");
				session.getNodeBuilder().setMaxFileSize(awaitIntParameter());
			}
		});

		add("Set MaxNumOfVersions", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set MaxNumOfVersions");
				System.out.println("Specify MaxNumOfVersions:\n");
				session.getNodeBuilder().setMaxNumOfVersions(awaitIntParameter());
			}
		});

		add("Set MaxSizeAllVersions", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set MaxSizeAllVersions");
				System.out.println("Specify MaxSizeAllVersions:\n");
				session.getNodeBuilder().setMaxSizeAllVersions(awaitIntParameter());
			}
		});

		add("Set ChunkSize", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set ChunkSize");
				System.out.println("Specify ChunkSize:\n");
				session.getNodeBuilder().setChunkSize(awaitIntParameter());
			}
		});

		add("Set AutostartProcesses", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set AutostartProcesses");
				System.out.println("Specify AutostartProcesses:\n");
				session.getNodeBuilder().setAutostartProcesses(Boolean.parseBoolean(awaitStringParameter()));
			}
		});

		add("Set IsMasterPeer", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set IsMasterPeer");
				System.out.println("Specify IsMasterPeer:\n");
				session.getNodeBuilder().setIsMaster(Boolean.parseBoolean(awaitStringParameter()));
			}
		});
		
		add("Set BootstrapAddress", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set BootstrapAddress");
				System.out.println("Specify BootstrapAddress:\n");
				try {
					session.getNodeBuilder().setBootstrapAddress(InetAddress.getByName(awaitStringParameter()));
				} catch (UnknownHostException e){
					System.out.println("UnknownHostException occured.");
					this.invoke();
				}
			}
		});

		add("Set RootPath", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set RootPath");
				System.out.println("Specify RootPath:\n");
				session.getNodeBuilder().setRootPath(awaitStringParameter());
			}
		});
		
		add("Create Network", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Create Network");
				createNetworkHandler();
			}
		});
		
		add("Create H2H Node", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Create H2H Node");
				session.setH2HNode(session.getNodeBuilder().build());
			}
		});
	}

	private void createNetworkHandler() {

		// TODO this whole procedure should exist as separate process

		// specify number of nodes
		System.out.println("Specify number of nodes:\n");
		int nrOfNodes = awaitIntParameter();
		if (nrOfNodes < 1) {
			System.out.println("Invalid number of nodes.\n");
			createNetworkHandler();
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
		return "Please select a network configuration option.\n";
	}

}
