package org.hive2hive.core.network.messages;

import java.security.PublicKey;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureResponse;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.futures.FutureRoutedListener;

public interface IMessageManager {

	/**
	 * Send a message which gets routed to the next responsible node according the
	 * {@link BaseMessage#getTargetKey()} key.</br>
	 * <b>Important:</b> This message gets encrypted with the given public key. Use this method for direct
	 * sending to nodes, which have the according private key.</br></br>
	 * <b>Design decision:</b>For an appropriate message handling like resends, error log and notifying
	 * listeners a {@link FutureRoutedListener} future listener gets attached to the {@link FutureDirect}
	 * object.
	 * 
	 * @param message
	 *            a message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 */
	boolean send(BaseMessage message, PublicKey targetPublicKey);

	/**
	 * Send a message directly to a node according the {@link BaseDirectMessage#getTargetAddress()} peer
	 * address.</br>
	 * <b>Important:</b> This message gets encrypted with the given public key. Use this method for direct
	 * sending to nodes, which have the according private key.</br></br>
	 * <b>Design decision:</b>For an appropriate message handling like resends, error log and notifying
	 * listeners a {@link FutureDirectListener} future listener gets attached to the {@link FutureResponse}
	 * object.
	 * 
	 * @param message
	 *            a direct message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 */
	boolean sendDirect(BaseDirectMessage message, PublicKey targetPublicKey);

}
