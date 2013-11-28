package org.hive2hive.core.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HNode;
import org.hive2hive.core.H2HNodeBuilder;
import org.hive2hive.core.network.NetworkManager;

public class NetworkMenu extends ConsoleMenu {

	public NetworkMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuHandlers() {

		add("Create Network", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Create Network");
				createNetworkHandler();
			}
		});
		
		add("Set MaxFileSize", new IConsoleMenuCallback() {
			public void invoke() {
  
			}
		});

		add("Set MaxNumOfVersions", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set MaxSizeAllVersions", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set ChunkSize", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set AutostartProcesses", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set IsMasterPeer", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set BootstrapAddress", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set RootPath", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});
	}
	
	private void createNetworkHandler() {
		
		// specify parameters
		// nr of nodes
		
		
		int nrOfNodes = 10;
		
		if (nrOfNodes < 1)
			throw new IllegalArgumentException("Invalid size of network.");
		List<NetworkManager> nodes = new ArrayList<NetworkManager>(nrOfNodes);
		

		// create the first node (master)
		NetworkManager master = new NetworkManager("master");
		master.connect();
		nodes.add(master);

		// create the other nodes and bootstrap them to the master peer
		char letter = 'A';
		for (int i = 1; i < nrOfNodes; i++) {
			NetworkManager node = new NetworkManager(String.format("node %s", ++letter));
			try {
				node.connect(InetAddress.getByName("127.0.0.1"));
			} catch (UnknownHostException e) {
				// should not happen
			}
			nodes.add(node);
		}
	}

	@Override
	public String getInstruction() {
		return "Please select a network configuration option.";
	}

}
