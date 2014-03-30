package org.hive2hive.core.network.data.parameters;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * An interface for the {@link DataManager} methods.
 * 
 * @author Seppi
 */
public interface IParameters {

	/**
	 * Get the non-hashed location key.
	 * 
	 * @return non-hased location key
	 */
	public String getLocationKey();

	/**
	 * Get the non-hashed domain key.
	 * 
	 * @return non-hashed domain key
	 */
	public String getDomainKey();

	/**
	 * Get the non-hashed content key.
	 * 
	 * @return non-hashed content key
	 */
	public String getContentKey();

	/**
	 * First dimension of <code>TomP2P</code> key resolution. A node which its node id is closest to the
	 * location key becomes responsible for this key.
	 * 
	 * @return hashed location key
	 */
	public Number160 getLKey();

	/**
	 * Second dimension of <code>TomP2P</code> key resolution. A network can have several domains.
	 * 
	 * @return hashed domain key
	 */
	public Number160 getDKey();

	/**
	 * Third dimension of <code>TomP2P</code> key resolution. On a node can be stored several data under the
	 * same location key.
	 * 
	 * @return hashed content key
	 */
	public Number160 getCKey();

	/**
	 * Forth dimension of <code>TomP2P</code> key resolution. Stored content on a node can have multiple versions.
	 * 
	 * @return hashed version key
	 */
	public Number160 getVersionKey();
	
	/**
	 * Get the key containing all four <code>TomP2P</code> key dimensions. 
	 * 
	 * @return key containing the location, domain, content and version key
	 */
	public Number640 getKey();
	
	/**
	 * All content in the network gets signed in order to prevent unauthorized overwrites or deletes.
	 * 
	 * @return content protection key pair
	 */
	public KeyPair getProtectionKeys();
	
	public NetworkContent getData();

}
