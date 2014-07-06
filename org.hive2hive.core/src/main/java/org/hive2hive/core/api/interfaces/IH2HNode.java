package org.hive2hive.core.api.interfaces;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.events.framework.interfaces.INetworkEventGenerator;

/**
 * The starting point for the Hive2Hive node. From this class, you can perform user management (login, logout,
 * ...) and file management (add, remove, share, ...).<br>
 * The user management and the file management are split for a better understanding. However, this interface
 * could easily be extended.<br>
 * For further information, check the Hive2Hive website (http://hive2hive.com/).
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface IH2HNode extends INetworkEventGenerator {

	/**
	 * Connect to the network. The connection kind is dependent on {@link INetworkConfiguration}.
	 * 
	 * @return <code>true</code> if the connection was successful, <code>false</code> otherwise
	 */
	boolean connect();

	/**
	 * Disconnect the node from the network.
	 * 
	 * @return <code>true</code> if the disconnection was successful, <code>false</code> otherwise
	 */
	boolean disconnect();

	/**
	 * Checks whether this {@link IH2HNode} is connected.
	 * 
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 */
	boolean isConnected();

	/**
	 * Returns the user management. In the user management, the user can register himself, login and logout.
	 * 
	 * @return the user management
	 */
	IUserManager getUserManager();

	/**
	 * Returns the file management. In the file management, the user can (after successful login) add, update,
	 * remove files.
	 * 
	 * @return the file management
	 */
	IFileManager getFileManager();

	/**
	 * Returns the {@link INetworkConfiguration} given at the node creation.
	 * 
	 * @return the network configuration
	 */
	INetworkConfiguration getNetworkConfiguration();

	/**
	 * Returns the {@link IFileConfiguration} given at the node creation.
	 * 
	 * @return the file configuration
	 */
	IFileConfiguration getFileConfiguration();

	/**
	 * Returns the {@link Peer} which can be used to implement custom actions.
	 * 
	 * @return the TomP2P peer. Before calling {@link IH2HNode#connect()}, this method returns null.
	 */
	Peer getPeer();
}
