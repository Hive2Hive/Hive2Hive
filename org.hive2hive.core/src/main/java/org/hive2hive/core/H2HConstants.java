package org.hive2hive.core;

import java.io.File;
import java.math.BigInteger;

import net.tomp2p.dht.StorageMemory;
import net.tomp2p.peers.Number160;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;

public interface H2HConstants {

	// TODO this interface should be more structured and documented in a consistent way
	// TODO there are constants that are not used anymore, remove

	// Default file configuration
	static final BigInteger MEGABYTES = BigInteger.valueOf(1024 * 1024);
	public static final BigInteger DEFAULT_MAX_FILE_SIZE = BigInteger.valueOf(25).multiply(MEGABYTES);// 25 MB
	public static final int DEFAULT_MAX_NUM_OF_VERSIONS = 100; // 100 versions
	public static final BigInteger DEFAULT_MAX_SIZE_OF_ALL_VERSIONS = DEFAULT_MAX_FILE_SIZE.multiply(BigInteger
			.valueOf(DEFAULT_MAX_NUM_OF_VERSIONS));// max_size * max_num
	public static final int DEFAULT_CHUNK_SIZE = MEGABYTES.intValue(); // 1 MB

	// standard port for the Hive2Hive network
	public static final int H2H_PORT = 4622;
	// store data onto slow peers (e.g. Android devices) or not
	public static final boolean STORE_DATA_SLOW_PEERS = false;

	// standard timeout for discovery
	public static final long DISCOVERY_TIMEOUT_MS = 10000;
	// standard timeout for bootstrapping
	public static final long BOOTSTRAPPING_TIMEOUT_MS = 10000;
	// standard timeout for peer disconnection
	public static final long DISCONNECT_TIMEOUT_MS = 10000;

	// the configuration file name (lying in the root directory of the node)
	public static final String META_FILE_NAME = "h2h.conf";

	// the trash directory, where deleted files are moved
	public static final File TRASH_DIRECTORY = new File(FileUtils.getTempDirectory(), "H2HTrash");

	// configurations for network messages
	public static final int MAX_MESSAGE_SENDING = 5;
	public static final int MAX_MESSAGE_SENDING_DIRECT = 3;

	// maximal numbers of versions kept in the DHT (see versionKey)
	public static final int MAX_VERSIONS_HISTORY = 5;

	// DHT content keys - these are used to distinguish the different data types
	// stored for a given key
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String USER_LOCATIONS = "USER_LOCATIONS";
	public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";
	public static final String FILE_CHUNK = "FILE_CHUNK";
	public static final String META_FILE = "META_FILE";

	// number of allowed tries to retry a put
	public static final int PUT_RETRIES = 3;
	// number of allowed tries to retry a confirm
	public static final int CONFIRM_RETRIES = 3;
	// number of allowed tries to retry a remove
	public static final int REMOVE_RETRIES = 3;

	// maximum wait time until any network operation should be answered by the other peer (for each retry).
	// This just serves as a fallback against infinite blocking when all other mechanisms fail.
	public static final int AWAIT_NETWORK_OPERATION_MS = 60000;
	// maximum delay to wait until peers have time to answer until they get removed from the locations
	public static final int CONTACT_PEERS_AWAIT_MS = 10000;
	// Slow peers need to have more time since they may be dependent on buffered relaying
	public static final int CONTACT_SLOW_PEERS_AWAIT_MS = 30000;

	// maximum delay to wait until a peer candidate replies whether a direct download is possible or not
	public static final int DIRECT_DOWNLOAD_AWAIT_MS = 10000;
	// maximum delay to wait when a peer candidate is currently overloaded
	public static final int DIRECT_DOWNLOAD_RETRY_MS = 30000;

	public static final String USER_PROFILE_TASK_DOMAIN = "USER-PROFILE-TASK";

	// default key used in the TomP2P framework
	public static final Number160 TOMP2P_DEFAULT_KEY = Number160.ZERO;

	/**
	 * Encryption Key Management
	 */
	// key length for asymmetric user key pair
	public static final RSA_KEYLENGTH KEYLENGTH_USER_KEYS = RSA_KEYLENGTH.BIT_2048;

	// key length for asymmetric meta document encryption
	public static final RSA_KEYLENGTH KEYLENGTH_META_FILE = RSA_KEYLENGTH.BIT_2048;

	// key length for asymmetric chunk encryption
	public static final RSA_KEYLENGTH KEYLENGTH_CHUNK = RSA_KEYLENGTH.BIT_2048;

	// key length for asymmetric protection / authentication keys
	public static final RSA_KEYLENGTH KEYLENGTH_PROTECTION = RSA_KEYLENGTH.BIT_1024;

	// key length for symmetric user profile encryption
	public static final AES_KEYLENGTH KEYLENGTH_USER_PROFILE = AES_KEYLENGTH.BIT_256;

	// key length for symmetric part of hybrid encryption
	public static final AES_KEYLENGTH KEYLENGTH_HYBRID_AES = AES_KEYLENGTH.BIT_256;

	/**
	 * Replication
	 */
	public static final boolean ENABLE_REPLICATION = true;
	public static final int REPLICATION_FACTOR = 5;
	public static final int REPLICATION_INTERVAL_MS = 30 * 1000;
	public static final String REPLICATION_STRATEGY = "nRoot"; // or 0Root
	public static final boolean REPLICATE_TO_SLOW_PEERS = STORE_DATA_SLOW_PEERS;

	/**
	 * TTL of Data in Network
	 */
	// period in milliseconds between successive ttl check task executions
	public static final int TTL_CHECK_INTERVAL_MS = StorageMemory.DEFAULT_STORAGE_CHECK_INTERVAL;

	/**
	 * TTL Refreshment Management
	 */
	// delay in milliseconds before first ttl refreshment task is to be executed
	public static final int TTL_REFRESHMENT_DELAY = 500;
	// period in milliseconds between successive ttl refreshment task executions
	public static final int TTL_REFRESHMENT_PERIOD = 1000;

	/**
	 * Download Manager
	 */
	// the number of concurrent downloads
	public static final int CONCURRENT_DOWNLOADS = 25;
	// the interval where a download fetches the locations of all users that possibly could have the file
	public static final int DOWNLOAD_LOCATIONS_INTERVAL_S = 120;
	// the maximum count the download of a chunk is retried
	public static final int MAX_RETRIES_DOWNLOAD_SAME_CHUNK = 10;
}
