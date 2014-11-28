package org.hive2hive.client.menu;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.relay.android.MessageBufferConfiguration;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.FileEventListener;
import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.JavaSerializer;

/**
 * The network configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class NodeMenu extends H2HConsoleMenu {

	private H2HConsoleMenuItem createNetworkMenuItem;
	private H2HConsoleMenuItem connectToExistingNetworkItem;
	private IH2HNode node;

	private BigInteger maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private BigInteger maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;

	public NodeMenu(MenuContainer menus) {
		super(menus);
	}

	@Override
	protected void createItems() {
		createNetworkMenuItem = new H2HConsoleMenuItem("Create New Network") {
			protected void execute() {
				buildNode();
				connectNode(NetworkConfiguration.createInitial(askNodeID()));
			}
		};

		connectToExistingNetworkItem = new H2HConsoleMenuItem("Connect to Existing Network") {
			protected void execute() throws UnknownHostException {
				String nodeID = askNodeID();

				print("Specify Bootstrap Address:");
				InetAddress bootstrapAddress = InetAddress.getByName(awaitStringParameter());

				String port = "default";
				if (isExpertMode) {
					print("Specify Bootstrap Port or enter 'default':");
					port = awaitStringParameter();
				}

				buildNode();
				if ("default".equalsIgnoreCase(port)) {
					connectNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
				} else {
					connectNode(NetworkConfiguration.create(nodeID, bootstrapAddress, Integer.parseInt(port)));
				}
			}
		};
	}

	@Override
	protected void addMenuItems() {

		if (isExpertMode) {
			add(new H2HConsoleMenuItem("Set MaxFileSize") {

				protected void execute() {
					print("Specify MaxFileSize:");
					maxFileSize = BigInteger.valueOf(awaitIntParameter());
				}
			});

			add(new H2HConsoleMenuItem("Set MaxNumOfVersions") {
				protected void execute() {
					print("Specify MaxNumOfVersions:");
					maxNumOfVersions = awaitIntParameter();
				}
			});

			add(new H2HConsoleMenuItem("Set MaxSizeAllVersions") {
				protected void execute() {
					print("Specify MaxSizeAllVersions:");
					maxSizeAllVersions = BigInteger.valueOf(awaitIntParameter());
				}
			});

			add(new H2HConsoleMenuItem("Set ChunkSize") {
				protected void execute() {
					print("Specify ChunkSize:");
					chunkSize = awaitIntParameter();
				}
			});

			add(new H2HConsoleMenuItem("Open Utils") {
				protected void execute() {
					new UtilMenu().open();
				}
			});
		}

		add(createNetworkMenuItem);
		add(connectToExistingNetworkItem);
	}

	@Override
	public String getInstruction() {
		if (isExpertMode) {
			return "Configure and set up your own network or connect to an existing one.";
		} else {
			return "Do you want to create a new network or connect to an existing one?";
		}
	}

	public IH2HNode getNode() {
		return node;
	}

	public boolean createNetwork() {
		if (getNode() == null) {
			H2HConsoleMenuItem.printPrecondition("You are not connected to a network. Connect to a network first.");
			open(isExpertMode);
		}
		return getNode() != null;
	}

	private void buildNode() {
		IFileConfiguration fileConfig = FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions,
				chunkSize);
		JavaSerializer serializer = new JavaSerializer();
		node = H2HNode.createNode(fileConfig, new H2HDefaultEncryption(serializer), serializer);
		node.getFileManager().subscribeFileEvents(new FileEventListener(node.getFileManager()));
	}

	private void connectNode(INetworkConfiguration networkConfig) {
		if (node.connect(networkConfig)) {

			// TODO testing only
			new PeerBuilderNAT(node.getPeer().peer()).gcmAuthenticationKey("AIzaSyC6j5SQYXCMM_ofHa7VLshnCgcnDptIsJY")
					.bufferConfiguration(new MessageBufferConfiguration().bufferAgeLimit(10 * 1000)).start();

			print("Network connection successfully established.");
			exit();
		} else {
			print("Network connection could not be established.");
		}
	}

	private String askNodeID() {
		String nodeID = UUID.randomUUID().toString();
		if (isExpertMode) {
			print("Specify Node ID:");
			nodeID = awaitStringParameter();
		}
		return nodeID;
	}

}
