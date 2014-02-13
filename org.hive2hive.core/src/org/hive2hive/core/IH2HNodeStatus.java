package org.hive2hive.core;

import java.io.File;

import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

/**
 * A status object that helps the developer to retrieve the node state. Who is logged in, where is the given
 * root, ... To get this values in real-time, the status object must be newly fetched. Once fetched, these
 * value does not update themselves.
 * 
 * @author Nico
 * 
 */
public interface IH2HNodeStatus {

	// TODO this interface should optimally not exist anymore after the API refactoring, rather should the
	// developer receive the informations from the different managers or from the H2HNode directly

	/**
	 * Get the file root that was given at the login call.
	 * 
	 * @return the file root or null if the user is not logged in
	 */
	File getRoot();

	/**
	 * Get the user id of the currently logged in user.
	 * 
	 * @return the user id or null, if no user is logged in
	 */
	String getUserId();

	/**
	 * Returns if the node of this machine is connected to a P2P network.
	 * 
	 * @return true when the peer is connected to the network, otherwise false
	 */
	boolean isConnected();

	/**
	 * Returns if a user is logged in.
	 * 
	 * @return true if a user is logged in, otherwise false
	 */
	boolean isLoggedIn();

	/**
	 * Returns the number of currently running processes (all asynchronous). Note that if you have multiple
	 * nodes in the same JVM, the number of processes are added. A process is a implementation of
	 * {@link IProcessComponent}.
	 * 
	 * @return the number of currently running processes (asynchronous)
	 */
	int getNumberOfProcesses();
}