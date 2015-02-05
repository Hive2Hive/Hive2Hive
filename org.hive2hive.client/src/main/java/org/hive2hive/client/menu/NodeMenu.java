package org.hive2hive.client.menu;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.relay.RelayType;
import net.tomp2p.relay.android.AndroidRelayServerConfig;
import net.tomp2p.relay.buffer.MessageBufferConfiguration;
import net.tomp2p.relay.tcp.buffered.BufferedTCPRelayServerConfig;

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
import org.hive2hive.core.security.FSTSerializer;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.IH2HSerialize;
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

				String bootstrapPort = "default";
				if (isExpertMode) {
					print("Specify Bootstrap Port or enter 'default':");
					bootstrapPort = awaitStringParameter();
				}

				buildNode();
				NetworkConfiguration config = NetworkConfiguration.create(nodeID, bootstrapAddress);
				if (!"default".equalsIgnoreCase(bootstrapPort)) {
					config.setBootstrapPort(Integer.parseInt(bootstrapPort));
				}

				connectNode(config);
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
		IH2HSerialize serializer;
		if ("java".equalsIgnoreCase(config.getString("Serializer.mode"))) {
			serializer = new JavaSerializer();
		} else {
			serializer = new FSTSerializer(config.getBoolean("Serializer.FST.unsafe"), "BC");
		}

		node = H2HNode.createNode(fileConfig, new H2HDefaultEncryption(serializer), serializer);
	}

	private void connectNode(NetworkConfiguration networkConfig) {
		String bindPort = "auto";
		if (isExpertMode) {
			print("Specify Port to bind this new peer or enter 'auto':");
			bindPort = awaitStringParameter();
		}

		if (!"auto".equalsIgnoreCase(bindPort)) {
			networkConfig.setPort(Integer.parseInt(bindPort));
		}

		if (node.connect(networkConfig)) {

			print("Network connection successfully established.");
			// connect the event bus
			node.getFileManager().subscribeFileEvents(new FileEventListener(node.getFileManager()));

			String address = config.getString("InetAddress");
			if (!"auto".equalsIgnoreCase(address)) {
				try {
					InetAddress inetAddress = InetAddress.getByName(address);
					print("Binding to address " + inetAddress);
					PeerAddress peerAddress = node.getPeer().peerBean().serverPeerAddress().changeAddress(inetAddress);
					node.getPeer().peerBean().serverPeerAddress(peerAddress);
				} catch (UnknownHostException e) {
					print("Cannot resolve address " + address);
				}
			}

			if (config.getBoolean("Relay.enabled")) {
				print("Starting relay functionality");
				String authenticationKey = config.getString("Relay.GCM.api-key");
				long bufferAge = config.getDuration("Relay.GCM.buffer-age-limit", TimeUnit.MILLISECONDS);

				MessageBufferConfiguration bufferConfiguration = new MessageBufferConfiguration().bufferAgeLimit(bufferAge);
				AndroidRelayServerConfig androidServer = new AndroidRelayServerConfig(authenticationKey, 5,
						bufferConfiguration);
				BufferedTCPRelayServerConfig tcpServer = new BufferedTCPRelayServerConfig(bufferConfiguration);

				new PeerBuilderNAT(node.getPeer().peer()).addRelayServerConfiguration(RelayType.ANDROID, androidServer)
						.addRelayServerConfiguration(RelayType.BUFFERED_OPENTCP, tcpServer).start();
			}
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
