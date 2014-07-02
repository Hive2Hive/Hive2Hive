package org.hive2hive.core.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.model.NetworkContent;

/**
 * Dummy encryption class that does not really encrypt but just serializes the object. This class should
 * <strong>not</strong> be used in productive environments because it does not provide any security!
 * 
 * @author Nico
 * 
 */
public class H2HDummyEncryption implements IH2HEncryption {

	@Override
	public EncryptedNetworkContent encryptAES(NetworkContent content, SecretKey aesKey) throws InvalidCipherTextException,
			IOException {
		return new EncryptedNetworkContent(SerializationUtil.serialize(content), new byte[] {});
	}

	@Override
	public NetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey) throws InvalidCipherTextException,
			ClassNotFoundException, IOException {
		return (NetworkContent) SerializationUtil.deserialize(content.getCipherContent());
	}

	@Override
	public HybridEncryptedContent encryptHybrid(NetworkContent content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException, IOException {
		return new HybridEncryptedContent(new byte[] {}, SerializationUtil.serialize(content));
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		return new HybridEncryptedContent(new byte[] {}, content);
	}

	@Override
	public NetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return (NetworkContent) SerializationUtil.deserialize(content.getEncryptedData());
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return content.getEncryptedData();
	}

}
