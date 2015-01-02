package org.hive2hive.core.security;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

import net.tomp2p.message.SignatureCodec;

import org.hive2hive.core.H2HConstants;

/**
 * A RSA encoder and decoder for the signature.
 * 
 * @author Thomas
 * @author Seppi
 * @author Nico
 */
public class H2HSignatureCodec implements SignatureCodec {

	// get the default byte count
	private static final int SIGNATURE_SIZE = H2HConstants.KEYLENGTH_PROTECTION.value() / 8;

	private byte[] encodedData;

	/**
	 * Create a signature codec using an already existing signature (encoded)
	 * 
	 * @param encodedData the encoded signature
	 * @throws IOException
	 */
	public H2HSignatureCodec(byte[] encodedData) throws IOException {
		if (encodedData.length != SIGNATURE_SIZE) {
			throw new IOException("RSA signature has size " + SIGNATURE_SIZE + " received: " + encodedData.length);
		}
		this.encodedData = encodedData;
	}

	/**
	 * Create a signature codec from a buffer
	 * 
	 * @param buf the buffer containing the signature at its reader index
	 */
	public H2HSignatureCodec(ByteBuf buf) {
		encodedData = new byte[SIGNATURE_SIZE];
		buf.readBytes(encodedData);
	}

	@Override
	public byte[] encode() {
		// no encoding necessary
		return encodedData;
	}

	@Override
	public SignatureCodec write(ByteBuf buf) {
		buf.writeBytes(encodedData);
		return this;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(encodedData);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof H2HSignatureCodec)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		H2HSignatureCodec s = (H2HSignatureCodec) obj;
		return Arrays.equals(s.encodedData, encodedData);
	}

}
