package org.hive2hive.core;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.security.EncryptionUtil;

public interface H2HConstants {

	// H2HNode default values
	public static final int DEFAULT_MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
	public static final int DEFAULT_MAX_NUM_OF_VERSIONS = 100;
	public static final int DEFAULT_MAX_SIZE_ALL_VERSIONS = DEFAULT_MAX_FILE_SIZE * DEFAULT_MAX_NUM_OF_VERSIONS;
	public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024; // 1 MB
	public static final boolean DEFAULT_AUTOSTART_PROCESSES = true;
	public static final boolean DEFAULT_IS_MASTER_PEER = false;
	public static final InetAddress DEFAULT_BOOTSTRAP_ADDRESS = null;
	public static final String DEFAULT_ROOT_PATH = new File(System.getProperty("user.home"), "Hive2Hive").getAbsolutePath();
	
	// standard port for the hive2hive network
	public static final int H2H_PORT = 4622;

	// define the default encoding char set (use explicitly)
	public static final Charset ENCODING_CHARSET = Charset.forName("UTF-8");

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
	public static final long MIN_VERSION_AGE_BEFORE_REMOVAL_MS = 5 * 60 * 1000; // 5mins

	// DHT content keys - these are used to distinguish the different data types
	// stored for a given key
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String USER_LOCATIONS = "USER_LOCATIONS";
	public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";
	public static final String USER_MESSAGE_QUEUE_KEY = "USER_MESSAGE_QUEUE_KEY";
	public static final String FILE_CHUNK = "FILE_CHUNK";
	public static final String META_DOCUMENT = "META_DOCUMENT";

	// waiting time (in milliseconds) after a put operation to verify if put succeeded
	public static final long PUT_VERIFICATION_WAITING_TIME_MS = 2000;

	// number of allowed tries to retry a put
	public static final int PUT_RETRIES = 3;
	// number of allowed tries to get for verification a put
	public static final int GET_RETRIES = 3;

	// maximum delay to wait until peers have time to answer until they get removed from the locations
	public static final long CONTACT_PEERS_AWAIT_MS = 10000;

	// key lengths for sending messages
	public static final EncryptionUtil.RSA_KEYLENGTH H2H_RSA_KEYLENGTH = EncryptionUtil.RSA_KEYLENGTH.BIT_512;
	public static final EncryptionUtil.AES_KEYLENGTH H2H_AES_KEYLENGTH = EncryptionUtil.AES_KEYLENGTH.BIT_128;

	public static final String UM_DOMAIN = "USER-MESSAGE";
	public static final String UM_CONTENT_KEY_PREFIX = "USER-MESSAGE";
}
