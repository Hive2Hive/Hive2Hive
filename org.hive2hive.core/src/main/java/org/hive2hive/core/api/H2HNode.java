package org.hive2hive.core.api;

import net.tomp2p.dht.PeerDHT;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.FSTSerializer;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.IH2HSerialize;

/**
 * Default implementation of {@link IH2HNode}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HNode implements IH2HNode {

	// TODO remove manager singletons
	// TODO atm, this class is just a wrapper for the NetworkManager
	private final IFileConfiguration fileConfiguration;
	private final NetworkManager networkManager;
	private final EventBus eventBus;

	private IUserManager userManager;
	private IFileManager fileManager;

	private H2HNode(IFileConfiguration fileConfiguration, IH2HEncryption encryption, IH2HSerialize serializer) {
		this.fileConfiguration = fileConfiguration;
		this.eventBus = new EventBus();
		this.networkManager = new NetworkManager(encryption, serializer, eventBus, fileConfiguration);
	}

	/**
	 * Create a Hive2Hive node instance. Before the node can be used, a {@link IH2HNode#connect()} must be
	 * called.
	 * 
	 * @param fileConfiguration the file configuration
	 * @return
	 */
	public static IH2HNode createNode(IFileConfiguration fileConfiguration) {
		FSTSerializer serializer = new FSTSerializer();
		return new H2HNode(fileConfiguration, new H2HDefaultEncryption(serializer), serializer);
	}

	/**
	 * Same as {@link H2HNode#createNode(IFileConfiguration)}, but with additional
	 * capability to provide an own encryption implementation
	 * 
	 * @param fileConfiguration the file configuration
	 * @param encryption and decryption implementation
	 * @param serializer the serialization implementation
	 * @return
	 */
	public static IH2HNode createNode(IFileConfiguration fileConfiguration, IH2HEncryption encryption,
			IH2HSerialize serializer) {
		return new H2HNode(fileConfiguration, encryption, serializer);
	}

	@Override
	public boolean connect(INetworkConfiguration networkConfiguration) {
		return networkManager.connect(networkConfiguration);
	}

	@Override
	public boolean connect(PeerDHT peer, boolean startReplication) {
		return networkManager.connect(peer, startReplication);
	}

	@Override
	public boolean disconnect() {
		return networkManager.disconnect();
	}

	@Override
	public boolean isConnected() {
		return networkManager.isConnected();
	}

	@Override
	public IUserManager getUserManager() {
		if (userManager == null) {
			userManager = new H2HUserManager(networkManager, eventBus);
		}
		return userManager;
	}

	@Override
	public IFileManager getFileManager() {
		if (fileManager == null) {
			fileManager = new H2HFileManager(networkManager, fileConfiguration, eventBus);
		}
		return fileManager;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	@Override
	public PeerDHT getPeer() {
		return networkManager.getConnection().getPeerDHT();
	}
}
