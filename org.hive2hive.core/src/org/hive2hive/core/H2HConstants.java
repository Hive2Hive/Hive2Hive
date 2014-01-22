package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;

import net.tomp2p.peers.Number160;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;

public interface H2HConstants {

	// TODO this interface should be more structured and documented in a consistent way

	// H2HNode default values
	public static final long DEFAULT_MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
	public static final long DEFAULT_MAX_NUM_OF_VERSIONS = 100;
	public static final long DEFAULT_MAX_SIZE_OF_ALL_VERSIONS = 25 * 1024 * 1024 * 100; // max_size * max_num
	public static final long DEFAULT_CHUNK_SIZE = 1024 * 1024; // 1 MB
	public static final boolean DEFAULT_AUTOSTART_PROCESSES = true;
	public static final boolean DEFAULT_IS_MASTER_PEER = false;
	public static final InetAddress DEFAULT_BOOTSTRAP_ADDRESS = null;
	public static final String DEFAULT_ROOT_PATH = new File(System.getProperty("user.home"), "Hive2Hive")
			.getAbsolutePath();

	// standard port for the Hive2Hive network
	public static final int H2H_PORT = 4622;

	// the configuration file name (lying in the root directory of the node)
	public static final String META_FILE_NAME = "h2h.conf";

	// the trash directory, where deleted files are moved
	public static final File TRASH_DIRECTORY = new File(FileUtils.getTempDirectory(), "H2HTrash");

	// configurations for network messages
	public static final int MAX_MESSAGE_SENDING = 5;
	public static final int MAX_MESSAGE_SENDING_DIRECT = 3;

	// enable/disable the put verification on the remote peer
	public static final boolean REMOTE_VERIFICATION_ENABLED = true;

	// maximal numbers of versions kept in the DHT (see versionKey)
	public static final int MAX_VERSIONS_HISTORY = 5;
	public static final long MIN_VERSION_AGE_BEFORE_REMOVAL_MS = 5 * 60 * 1000; // 5 mins

	// DHT content keys - these are used to distinguish the different data types
	// stored for a given key
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String USER_LOCATIONS = "USER_LOCATIONS";
	public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";
	public static final String USER_MESSAGE_QUEUE_KEY = "USER_MESSAGE_QUEUE_KEY";
	public static final String FILE_CHUNK = "FILE_CHUNK";
	public static final String META_DOCUMENT = "META_DOCUMENT";

	// waiting time (in ms) after a put operation to verify if put succeeded
	public static final long PUT_VERIFICATION_WAITING_TIME_MS = 2000;

	public static final int PUT_RETRIES = 3; // number of allowed tries to retry a put
	public static final int REMOVE_RETRIES = 3; // number of allowed tries to retry a remove
	public static final int GET_RETRIES = 3; // number of allowed tries to retry a get

	// maximum delay to wait until peers have time to answer until they get removed from the locations
	public static final long CONTACT_PEERS_AWAIT_MS = 10000;

	public static final String USER_PROFILE_TASK_DOMAIN = "USER-PROFILE-TASK";

	// default key used in the TomP2P framework
	public static final Number160 TOMP2P_DEFAULT_KEY = Number160.ZERO;

	// number of threads that netty / tomp2p are allowed to have. Too few threads can lead to slow response
	// times, too many threads can exceed the available memory
	public static final int NUM_OF_NETWORK_THREADS = 32;

	/**
	 * Encryption Key Management
	 */
	// key length for asymmetric user key pair
	public static final RSA_KEYLENGTH KEYLENGTH_USER_KEYS = RSA_KEYLENGTH.BIT_2048;

	// key length for asymmetric meta document encryption
	public static final RSA_KEYLENGTH KEYLENGTH_META_DOCUMENT = RSA_KEYLENGTH.BIT_2048;

	// key length for asymmetric chunk encryption
	public static final RSA_KEYLENGTH KEYLENGTH_CHUNK = RSA_KEYLENGTH.BIT_2048;

	// key length for symmetric user profile encryption
	public static final AES_KEYLENGTH KEYLENGTH_USER_PROFILE = AES_KEYLENGTH.BIT_256;

	// key length for symmetric part of hybrid encryption
	public static final AES_KEYLENGTH KEYLENGTH_HYBRID_AES = AES_KEYLENGTH.BIT_256;

}
