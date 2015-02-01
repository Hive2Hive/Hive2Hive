package org.hive2hive.core.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;

/**
 * Dummy encryption class that does not really encrypt but just serializes the object. This class should
 * <strong>not</strong> be used in productive environments because it does not provide any security!
 * 
 * @author Nico
 * 
 */
public class H2HDummyEncryption implements IH2HEncryption {

	private final IH2HSerialize serializer;

	public H2HDummyEncryption() {
		serializer = new FSTSerializer();
	}

	@Override
	public EncryptedNetworkContent encryptAES(BaseNetworkContent content, SecretKey aesKey)
			throws InvalidCipherTextException, IOException {
		return new EncryptedNetworkContent(serializer.serialize(content), new byte[] {});
	}

	@Override
	public BaseNetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey)
			throws InvalidCipherTextException, ClassNotFoundException, IOException {
		return (BaseNetworkContent) serializer.deserialize(content.getCipherContent());
	}

	@Override
	public HybridEncryptedContent encryptHybrid(BaseNetworkContent content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException, IOException {
		return new HybridEncryptedContent(new byte[] {}, serializer.serialize(content));
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		return new HybridEncryptedContent(new byte[] {}, content);
	}

	@Override
	public BaseNetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException,
			ClassNotFoundException, IOException {
		return (BaseNetworkContent) serializer.deserialize(content.getEncryptedData());
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return content.getEncryptedData();
	}

}
