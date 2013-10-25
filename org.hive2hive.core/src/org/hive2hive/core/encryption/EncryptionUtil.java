package org.hive2hive.core.encryption;

import java.security.Security;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class EncryptionUtil {

	private static String digits = "0123456789abcdef";
	
	private static final String BC = "BC";
	private static final String AES_CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding";

	private EncryptionUtil() {

	}

	public static byte[] generateIV(){
		return null;
	}
	
	public static void encryptAES(byte[] data, SecretKey secretKey) {

	}

	public static void encryptAES(byte[] data, byte[] initVector, SecretKey secretKey) {

		// initialize the initialization vector (IV)
		IvParameterSpec ivSpec = new IvParameterSpec(initVector);
	}

	public static String toHex(byte[] data) {

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i != data.length; i++) {
			int v = data[i] & 0xff;

			buf.append(digits.charAt(v >> 4));
			buf.append(digits.charAt(v & 0xf));
		}

		return buf.toString();
	}

	public static void installBCProvider() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}
}
