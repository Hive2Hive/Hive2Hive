package org.hive2hive.core.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class ProfileEncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ProfileEncryptionUtil.class);

	private ProfileEncryptionUtil() {
	}

//	public static String createEPassFromPassword(String aPassword) {
//		Digester digester = new Digester();
//		digester.setAlgorithm("SHA-1");
//		byte[] digest = digester.digest(EncryptionUtil.toByte(aPassword));
//		return EncryptionUtil.toString(digest);
//	}
//
//	public static ClosedUserProfile encrypt(User aUser) {
//		ClosedUserProfile closedUserProfile = ModelFactory.eINSTANCE.createClosedUserProfile();
//		closedUserProfile.setUserID(aUser.getUserID());
//		closedUserProfile.setPublicKey(convertKeyToString(aUser.getProfile().getPublicKey(), true));
//
//		byte[] aesKey = createRandomAESKey();
//		SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
//		try {
//			Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
//			try {
//				cipher.init(Cipher.ENCRYPT_MODE, keySpec);
//				try {
//					byte[] encodedPrivateKey = cipher.doFinal(toByte(convertKeyToString(aUser.getProfile()
//							.getPrivateKey(), false)));
//					closedUserProfile.setPrivateKey(toString(encodedPrivateKey));
//
//					// encrypt file tree
//					Resource rs = ModelUtil.createResource("save");
//					rs.getContents().add(aUser.getProfile().getFileRoot());
//					String fileTreeAsString = ModelUtil.resourceToString(rs, UTF_8);
//					String encryptedFileTreeAsString = toString(cipher.doFinal(toByte(fileTreeAsString)));
//					closedUserProfile.setFileGhost(encryptedFileTreeAsString);
//
//					// encrypt friend lists
//					ArrayList<Friend> friends = new ArrayList<Friend>(aUser.getProfile().getFriends());
//					closedUserProfile.setFriends(new EncryptionCapsule(friends, aUser.getProfile()
//							.getPublicKey()));
//					ArrayList<Friend> friendRequests = new ArrayList<Friend>(aUser.getProfile()
//							.getFriendRequests());
//					closedUserProfile.setFriendRequests(new EncryptionCapsule(friendRequests, aUser
//							.getProfile().getPublicKey()));
//					ArrayList<Friend> pendingFriendRequests = new ArrayList<Friend>(aUser.getProfile()
//							.getPendingFriendRequests());
//					closedUserProfile.setPendingFriendRequests(new EncryptionCapsule(pendingFriendRequests,
//							aUser.getProfile().getPublicKey()));
//
//					byte[] salt = createRandomSalt();
//					closedUserProfile.setTempKey(encryptTempKey(aesKey, aUser.getPassword(), salt));
//					closedUserProfile.setPrivateSalt(toString(salt));
//					closedUserProfile.setEPass(aUser.getProfile().getEPass());
//					closedUserProfile.setIv(toString(cipher.getIV()));
//				} catch (IllegalBlockSizeException | BadPaddingException e) {
//					logger.error("Exception during encryption:", e);
//				}
//			} catch (InvalidKeyException e) {
//				logger.error("Invalide key:", e);
//				e.printStackTrace();
//			}
//
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//			logger.error("Error during cipher initialisation:", e);
//		}
//
//		return closedUserProfile;
//	}
//
//	// @SuppressWarnings("unchecked")
//	public static UserProfile decrypt(ClosedUserProfile aClosedProfile, User aUser) {
//		UserProfile userProfile = ModelFactory.eINSTANCE.createUserProfile();
//
//		byte[] aesKey = decryptTempKey(aClosedProfile.getTempKey(), aUser.getPassword(),
//				aClosedProfile.getPrivateSalt());
//		SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
//		try {
//			Cipher c = Cipher.getInstance(AES_CIPHER_MODE);
//			try {
//				c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(toByte(aClosedProfile.getIv())));
//				try {
//					// decrypt private key
//					String privateKey = toString(c.doFinal(toByte(aClosedProfile.getPrivateKey())));
//					userProfile.setPrivateKey((PrivateKey) convertStringToKey(privateKey, false));
//					userProfile.setPublicKey((PublicKey) convertStringToKey(aClosedProfile.getPublicKey(),
//							true));
//					userProfile.setEPass(aClosedProfile.getEPass());
//
//					// decrypt file tree
//					String decryptedFileTreeAsString = toString(c.doFinal(toByte(aClosedProfile
//							.getFileGhost())));
//					Resource fileTreeResource = ModelUtil.stringToResource(decryptedFileTreeAsString, UTF_8);
//					Directory fileTreeRoot = (Directory) fileTreeResource.getContents().get(0);
//					userProfile.setFileRoot(fileTreeRoot);
//
//					// decrypt friend lists
//					userProfile.getFriends().addAll(
//							(ArrayList<Friend>) aClosedProfile.getFriends().getContent(
//									userProfile.getPrivateKey()));
//					userProfile.getFriendRequests().addAll(
//							(ArrayList<Friend>) aClosedProfile.getFriendRequests().getContent(
//									userProfile.getPrivateKey()));
//					userProfile.getPendingFriendRequests().addAll(
//							(ArrayList<Friend>) aClosedProfile.getPendingFriendRequests().getContent(
//									userProfile.getPrivateKey()));
//				} catch (IllegalBlockSizeException | BadPaddingException e) {
//					logger.error("Exception during encryption:", e);
//				}
//			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//				logger.error("Exception during cipher initialisation:", e);
//			}
//
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//			logger.error("Error during cipher initialisation:", e);
//		}
//
//		return userProfile;
//	}
//
//	private static String encryptTempKey(byte[] aesKey, String aPassword, byte[] someSalt) {
//		try {
//			SecretKey desKey = createDESKey(aPassword, someSalt);
//			Cipher cipher = Cipher.getInstance("DES");
//			cipher.init(Cipher.ENCRYPT_MODE, desKey);
//			return toString(cipher.doFinal(aesKey));
//		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
//				| IllegalBlockSizeException | BadPaddingException e) {
//			logger.error("Exception during temp key encryption:", e);
//		}
//		return null;
//	}
//
//	private static byte[] decryptTempKey(String aKeyAsString, String aPassword, String someSalt) {
//		try {
//			SecretKey desKey = createDESKey(aPassword, toByte(someSalt));
//			Cipher cipher = Cipher.getInstance("DES");
//			cipher.init(Cipher.DECRYPT_MODE, desKey);
//			return cipher.doFinal(toByte(aKeyAsString));
//		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
//				| IllegalBlockSizeException | BadPaddingException e) {
//			logger.error("Exception during temp key decryption:", e);
//		}
//		return null;
//	}
}
