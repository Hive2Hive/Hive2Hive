package org.hive2hive.core.network.data.download.direct.process;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestChunkMessage extends DirectRequestMessage {

	private static final long serialVersionUID = 3591235525796608138L;
	private static final Logger logger = LoggerFactory.getLogger(ContactPeerMessage.class);

	private final PublicKey fileKey;
	private final int chunkNumber; // the index / order number of the chunk
	private final int chunkLength; // how many bytes to read
	private final byte[] chunkHash; // the md5 hash of the file

	public RequestChunkMessage(PeerAddress targetPeerAddress, PublicKey fileKey, int chunkNumber,
			int chunkLength, byte[] chunkHash) {
		super(targetPeerAddress);
		this.fileKey = fileKey;
		this.chunkNumber = chunkNumber;
		this.chunkLength = chunkLength;
		this.chunkHash = chunkHash;
	}

	@Override
	public void run() {
		logger.debug("Received request for a chunk from peer {}", senderAddress);

		// search user profile for this file
		H2HSession session = null;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("Cannot answer because session is invalid");
			sendDirectResponse(createResponse(null));
			return;
		}

		UserProfile userProfile;
		try {
			UserProfileManager profileManager = session.getProfileManager();
			userProfile = profileManager.getUserProfile(messageID, false);
		} catch (GetFailedException e) {
			logger.error("Cannot get the user profile", e);
			sendDirectResponse(createResponse(null));
			return;
		}

		// find file in user profile
		Index index = userProfile.getFileById(fileKey);
		if (index == null || index.isFolder()) {
			logger.info("File not found in the user profile, cannot return a chunk");
			sendDirectResponse(createResponse(null));
			return;
		}

		// check if file is on disk
		Path path = FileUtil.getPath(session.getRoot(), index);
		if (path == null || !path.toFile().exists()) {
			logger.info("File not found on disk, cannot return a chunk");
			sendDirectResponse(createResponse(null));
			return;
		}

		Chunk chunk = null;
		try {
			// retrieve the requested file part (offset and length)
			chunk = FileChunkUtil.getChunk(path.toFile(), chunkLength, chunkNumber, "chunk-" + chunkNumber);
		} catch (IOException e) {
			logger.error("Cannot read the chunk", e);
			sendDirectResponse(createResponse(null));
			return;
		}

		// verify the chunk hash
		byte[] md5Hash = EncryptionUtil.generateMD5Hash(chunk.getData());
		if (H2HEncryptionUtil.compareMD5(md5Hash, chunkHash)) {
			logger.debug("MD5 hash of the chunk {} has been verified, returning the chunk", chunkNumber);

			// return the content of the file part
			sendDirectResponse(createResponse(chunk));
		} else {
			logger.warn("MD5 hash of the read chunk {} and of the expected file does not match", chunkNumber);
			sendDirectResponse(createResponse(null));
		}
	}
}
