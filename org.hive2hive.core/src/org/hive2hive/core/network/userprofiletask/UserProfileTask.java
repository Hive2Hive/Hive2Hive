package org.hive2hive.core.network.userprofiletask;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;

import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * The base class of all {@link UserProfileTask}s.</br>
 * An encrypted and signed task ({@link Runnable}) which is stored on
 * the proxy node of the receiving user. This task will be stored in a “queue”-like data structure. This
 * allows an asynchronous communication between users (i.e., between friends).</br>
 * This task is used in case a client needs to update its profile< due to changes introduced by
 * friends.
 * 
 * @author Christian
 * 
 */
public abstract class UserProfileTask extends NetworkContent implements Runnable {

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileTask.class);

	private static final long serialVersionUID = -773794512479641000L;

	private byte[] objectSignState; // serialized state of this object when signed
	private byte[] signature;

	/**
	 * Sign this user message such that the receiver can verify the sender with the senders public key.
	 */
	public final void sign(PrivateKey senderPrivateKey) {

		byte[] signatureState = EncryptionUtil.serializeObject(this);
		try {
			signature = EncryptionUtil.sign(signatureState, senderPrivateKey);
		} catch (InvalidKeyException | SignatureException e) {
			logger.error("Exception while signing user message: ", e);
		}
	}

	/**
	 * Verify this user message such that the sender can be uniquely identified.
	 * 
	 * @param senderPublicKey The public key of the assumed/accepted sender.
	 */
	public final boolean verify(PublicKey senderPublicKey) {
		try {
			return EncryptionUtil.verify(objectSignState, signature, senderPublicKey);
		} catch (InvalidKeyException | SignatureException e) {
			logger.error("Exception while verifying user message: ", e);
		}
		return false;
	}

	/**
	 * Start the execution of this user message.
	 */
	public final void start() {
		new Thread(this).start();
	}

	public final void run() {
		execute();
	}

	/**
	 * The execution part of this user message.
	 */
	protected abstract void execute();

	public final byte[] getSignature() {
		return signature;
	}

	/**
	 * Creates a key which has a prefix (see {@link H2HConstants#USER_PROFILE_TASK_CONTENT_KEY_PREFIX} and a time stamp
	 * (taking current time).
	 * 
	 * @return a key
	 */
	public Number160 generateContentKey() {
		String prefix = H2HConstants.USER_PROFILE_TASK_CONTENT_KEY_PREFIX;
		// get the current time
		long timestamp = new Date().getTime();
		StringBuilder build = new StringBuilder();
		String contentKey = build.append(prefix).append(timestamp).toString();
		return Number160.createHash(contentKey);
	}
}
