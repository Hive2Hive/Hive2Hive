package org.hive2hive.core.network.data;

/**
 * Some {@link NetworkContent} can be shared among several users. To optimize the content protection
 * mechanisms all shared data has to store their hashes, which get produced while signing the data (see
 * {@link DataManager#put(net.tomp2p.peers.Number160, net.tomp2p.peers.Number160, net.tomp2p.peers.Number160, NetworkContent, java.security.KeyPair)}
 * ). The idea is that in case of changing the content protection keys there should be no need to download the
 * corresponding data, resign them and upload the resigned data again. To avoid the down/uploads
 * <code>H2H</code> stores the hashes, produced during signing. Thanks to this hashes it is possible to sign
 * only a put meta message (call of <code>TomP2P</code>), which changes the content protection keys.
 * 
 * @author Seppi
 */
public abstract class SharableNetworkContent extends NetworkContent {

	private static final long serialVersionUID = 1L;

	private byte[] hash;

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public byte[] getHash() {
		return this.hash;
	}

}
