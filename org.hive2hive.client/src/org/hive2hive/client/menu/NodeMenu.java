package org.hive2hive.client.menu;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

/**
 * The network configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class NodeMenu extends H2HConsoleMenu {

	public H2HConsoleMenuItem ConnectToExistingNetworkItem;
	public H2HConsoleMenuItem CreateNetworkMenuItem;
	
	private IH2HNode node;
	
	private BigInteger maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private BigInteger maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;

	@Override
	protected void createItems() {
		ConnectToExistingNetworkItem = new H2HConsoleMenuItem("Connect to Existing Network") {
			protected void execute() throws UnknownHostException {

				String nodeID = askNodeID();

				System.out.println("Specify Bootstrap Address:");
				InetAddress bootstrapAddress = InetAddress.getByName(awaitStringParameter());

				String port = "default";
				if (isExpertMode) {
					System.out.println("Specify Bootstrap Port or enter 'default':");
					port = awaitStringParameter();
				}
				if (port.equalsIgnoreCase("default")) {
					createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
				} else {
					createNode(NetworkConfiguration.create(nodeID, bootstrapAddress, Integer.parseInt(port)));
				}
				
				exit();
			}
		};

		CreateNetworkMenuItem = new H2HConsoleMenuItem("Create New Network") {
			protected void execute() {
				
				String nodeID = askNodeID();
				createNode(NetworkConfiguration.create(nodeID));
				
				exit();
			}
		};
	}

	@Override
	protected void addMenuItems() {

		if (isExpertMode) {
			add(new H2HConsoleMenuItem("Set MaxFileSize") {

				protected void execute() {
					System.out.println("Specify MaxFileSize:\n");
					maxFileSize = BigInteger.valueOf(awaitIntParameter());
				}
			});

			add(new H2HConsoleMenuItem("Set MaxNumOfVersions") {
				protected void execute() {
					System.out.println("Specify MaxNumOfVersions:\n");
					maxNumOfVersions = awaitIntParameter();
				}
			});

			add(new H2HConsoleMenuItem("Set MaxSizeAllVersions") {
				protected void execute() {
					System.out.println("Specify MaxSizeAllVersions:\n");
					maxSizeAllVersions = BigInteger.valueOf(awaitIntParameter());
				}
			});

			add(new H2HConsoleMenuItem("Set ChunkSize") {
				protected void execute() {
					System.out.println("Specify ChunkSize:\n");
					chunkSize = awaitIntParameter();
				}
			});
			
			add(new H2HConsoleMenuItem("Open Utils") {
				protected void execute() {
					new UtilMenu().open();
				}
			});
		}
		
		add(CreateNetworkMenuItem);
		add(ConnectToExistingNetworkItem);
	}

	@Override
	public String getInstruction() {
		
		if (isExpertMode)
			return "Configure and set up your own network or connect to an existing one.";
		else
			return "Do you want to create a new network or connect to an existing one?";
	}

	public IH2HNode getNode() {
		return node;
	}

	private void createNode(INetworkConfiguration networkConfig) {
		node = H2HNode.createNode(networkConfig,
				FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize));
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
		node.connect();
	}
	
	public void disconnectNode() {
		if (node != null) {
			node.disconnect();
		}
	}
	
	public void forceNetwork() {
		
		while (getNode() == null) {
			H2HConsoleMenuItem.printPreconditionError("You are not connected to a network. Connect to a network first.");
			open(isExpertMode);
		}
	}

	private String askNodeID() {
		String nodeID = UUID.randomUUID().toString();
		if (isExpertMode) {
			System.out.println("Specify Node ID:");
			nodeID = awaitStringParameter();
		}
		return nodeID;
	}
	
}
