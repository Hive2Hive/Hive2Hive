package org.hive2hive.core.security;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.tomp2p.message.RSASignatureCodec;

import org.hive2hive.core.H2HConstants;

/**
 * A RSA encoder and decoder for the signature.
 * 
 * @author Thomas
 * @author Seppi
 * @author Nico
 */
public class H2HSignatureCodec extends RSASignatureCodec {

	// get the default byte count
	public static final int SIGNATURE_SIZE = H2HConstants.KEYLENGTH_PROTECTION.value() / 8;

	public H2HSignatureCodec(byte[] encodedData) throws IOException {
		super(encodedData);
	}

	public H2HSignatureCodec(ByteBuf buf) {
		super(buf);
	}

	@Override
	public int signatureSize() {
		return SIGNATURE_SIZE;
	}
}
