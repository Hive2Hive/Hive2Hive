package org.hive2hive.core.network.data.parameters;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.DataManager;

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
	 * Forth dimension of <code>TomP2P</code> key resolution. Stored content on a node can have multiple
	 * versions.
	 * 
	 * @return hashed version key
	 */
	public Number160 getVersionKey();

	/**
	 * Reference to predecessor version, if any.
	 * 
	 * @return hashed based on key
	 */
	public Number160 getBasedOnKey();

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

	/**
	 * Get data which gets stored in the network
	 * 
	 * @return data to store
	 */
	public BaseNetworkContent getNetworkContent();

	/**
	 * All content in the network has a time-to-live value (in seconds). If data is expired it gets
	 * automatically removed from network.
	 * 
	 * @return time-to-live value in seconds
	 */
	public int getTTL();

	/**
	 * Sometimes the content protection keys have to be changed.
	 * 
	 * @return new content protection keys
	 */
	public KeyPair getNewProtectionKeys();

	/**
	 * Set a flag which indicates the signature procedure to store the created hash.
	 * 
	 * @param hashFlag <code>true</code> for storing the hash, otherwise <code>false</code>
	 * @return it-self (builder pattern)
	 */
	public IParameters setHashFlag(boolean hashFlag);

	/**
	 * Set a flag which indicates that the signature procedure has to store the created hash.
	 * 
	 * @return <code>true</code> if hash has to be stored, otherwise <code>false</code>
	 */
	public boolean getHashFlag();

	/**
	 * Set the hash of some data which has been signed in front of putting in network.
	 * 
	 * @param hash of the signed data
	 * @return it-self (builder pattern)
	 */
	public IParameters setHash(byte[] hash);

	/**
	 * Get the hash of the signed data.
	 * 
	 * @return hash of the signed data
	 */
	public byte[] getHash();

	/**
	 * Set data which is used to put in network.
	 * 
	 * @param data used to put in network
	 */
	public IParameters setData(Data data);

	/**
	 * Get the data object itself.
	 * 
	 * @return data used to put in network
	 */
	public Data getData();

	/**
	 * Prepare flag indicating if data should be stored only temporary. Prepared data has to be rejected or
	 * confirmed at a later time.
	 * 
	 * @return <code>true</code> if data is prepared, otherwise <code>false</code>
	 */
	public boolean hasPrepareFlag();

}
