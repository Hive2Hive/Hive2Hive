package org.hive2hive.client.menu;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.Console;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.H2HNodeBuilder;
import org.hive2hive.core.IH2HNode;

/**
 * The network configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class NodeCreationMenu extends ConsoleMenu {

	public H2HConsoleMenuItem ConnectToExistingNetworkItem;
	public H2HConsoleMenuItem CreateNetworkMenuItem;

	private IH2HNode h2hNode;
	private final H2HNodeBuilder nodeBuilder;

	public NodeCreationMenu(Console console) {
		super(console);
		nodeBuilder = new H2HNodeBuilder();

		// config that cannot be changed
		nodeBuilder.setAutostartProcesses(true);
	}

	@Override
	protected void createItems() {
		ConnectToExistingNetworkItem = new H2HConsoleMenuItem("Connect to existing network") {
			protected void execute() throws UnknownHostException {
				System.out.println("Specify BootstrapAddress:\n");
				InetAddress bootstrapAddress = InetAddress.getByName(awaitStringParameter());
				nodeBuilder.setBootstrapAddress(bootstrapAddress);
				nodeBuilder.setIsMaster(false);
				createNode();
			}
		};

		CreateNetworkMenuItem = new H2HConsoleMenuItem("Create new network") {
			protected void execute() {
				nodeBuilder.setIsMaster(true);
				createNode();
			}
		};
	}

	private void createNode() {
		// creates the node
		h2hNode = nodeBuilder.build();
		console.setH2HNode(h2hNode);
	}

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Set MaxFileSize") {
			protected void execute() {
				System.out.println("Specify MaxFileSize:\n");
				nodeBuilder.setMaxFileSize(awaitIntParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set MaxNumOfVersions") {
			protected void execute() {
				System.out.println("Specify MaxNumOfVersions:\n");
				nodeBuilder.setMaxNumOfVersions(awaitIntParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set MaxSizeAllVersions") {
			protected void execute() {
				System.out.println("Specify MaxSizeAllVersions:\n");
				nodeBuilder.setMaxSizeAllVersions(awaitIntParameter());
			}
		});

		add(new H2HConsoleMenuItem("Set ChunkSize") {
			protected void execute() {
				System.out.println("Specify ChunkSize:\n");
				nodeBuilder.setChunkSize(awaitIntParameter());
			}
		});

		add(ConnectToExistingNetworkItem);
		add(CreateNetworkMenuItem);
	}

	@Override
	public String getInstruction() {
		return "Configure the H2H node and connect to (or create) the network.";
	}

	public IH2HNode getH2HNode() {
		return h2hNode;
	}
}
