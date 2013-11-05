package org.hive2hive.core;

import org.hive2hive.core.process.IProcess;

/**
 * Interface for all operations on a Hive2Hive peer. Note that all calls are returned immediately although the
 * process may still be running in the background. The returned process object can be used to control
 * the call.
 * 
 * @author Nico
 * 
 */
public interface IH2HNode {

	IProcess register(String userId, String password, String pin);

	IProcess login(String userId, String password, String pin);

	void disconnect();
}
