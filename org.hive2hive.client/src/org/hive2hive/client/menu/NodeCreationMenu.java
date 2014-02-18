package org.hive2hive.client.menu;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
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
public final class NodeCreationMenu extends ConsoleMenu {

	private IH2HNode node;

	private long maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private long maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private long maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private long chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;

	public H2HConsoleMenuItem ConnectToExistingNetworkItem;
	public H2HConsoleMenuItem CreateNetworkMenuItem;

	@Override
	protected void createItems() {
		ConnectToExistingNetworkItem = new H2HConsoleMenuItem("Connect to Existing Network") {
			protected void execute() throws UnknownHostException {
				// System.out.println("Specify Node ID:\n");
				// String nodeID = awaitStringParameter();
				String nodeID = UUID.randomUUID().toString();

				System.out.println("Specify Bootstrap Address:\n");
				InetAddress bootstrapAddress = InetAddress.getByName(awaitStringParameter());

				System.out.println("Specify Bootstrap Port or enter 'default':\n");
				String port = awaitStringParameter();
				if ("default".equalsIgnoreCase(port)) {
					createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
				} else {
					createNode(NetworkConfiguration.create(nodeID, bootstrapAddress, Integer.parseInt(port)));
				}
			}
		};

		CreateNetworkMenuItem = new H2HConsoleMenuItem("Create New Network") {
			protected void execute() {
				// System.out.println("Specify Node ID:\n");
				// String nodeID = awaitStringParameter();
				String nodeID = UUID.randomUUID().toString();
				createNode(NetworkConfiguration.create(nodeID));
			}
		};
	}

	private void createNode(INetworkConfiguration networkConfig) {
		node = H2HNode.createNode(networkConfig,
				FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize));
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
		node.connect();
	}

	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Open Utils") {
			protected void execute() {
				new UtilMenu().open();
			}
		});
		add(new H2HConsoleMenuItem("Set MaxFileSize") {

			protected void execute() {
				System.out.println("Specify MaxFileSize:\n");
				maxFileSize = Long.parseLong(awaitStringParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set MaxNumOfVersions") {
			protected void execute() {
				System.out.println("Specify MaxNumOfVersions:\n");
				maxNumOfVersions = Long.parseLong(awaitStringParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set MaxSizeAllVersions") {
			protected void execute() {
				System.out.println("Specify MaxSizeAllVersions:\n");
				maxSizeAllVersions = Long.parseLong(awaitStringParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set ChunkSize") {
			protected void execute() {
				System.out.println("Specify ChunkSize:\n");
				chunkSize = Long.parseLong(awaitStringParameter());
			}
		});

		add(ConnectToExistingNetworkItem);
		add(CreateNetworkMenuItem);
	}

	@Override
	public String getInstruction() {
		return "Configure the H2H node and connect to (or create) the network.\n";
	}

	public IH2HNode getH2HNode() {
		return node;
	}
}
