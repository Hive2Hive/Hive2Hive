package org.hive2hive.core.processes.files.download.direct.process;

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
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.hive2hive.core.processes.files.download.direct.process.ChunkMessageResponse.AnswerType;
import org.hive2hive.core.security.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestChunkMessage extends DirectRequestMessage {

	private static final long serialVersionUID = 3591235525796608138L;
	private static final Logger logger = LoggerFactory.getLogger(RequestChunkMessage.class);

	private final PublicKey fileKey;
	// the index / order number of the chunk
	private final int chunkNumber;
	// how many bytes to read
	private final int chunkLength;
	// the md5 hash of the file
	private final byte[] chunkHash;

	public RequestChunkMessage(PeerAddress targetPeerAddress, PublicKey fileKey, int chunkNumber, int chunkLength,
			byte[] chunkHash) {
		super(targetPeerAddress);
		this.fileKey = fileKey;
		this.chunkNumber = chunkNumber;
		this.chunkLength = chunkLength;
		this.chunkHash = chunkHash;
	}

	@Override
	public void run() {
		logger.debug("Received request for a chunk from peer {}", senderAddress);

		// check for free heap space before reading the file
		long freeMemory = Runtime.getRuntime().freeMemory();
		if (freeMemory < 1.5 * chunkLength) {
			// not enough memory
			logger.error("Cannot read the chunk because not enough memory available");
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.ASK_LATER)));
			return;
		}

		// search user profile for this file
		H2HSession session = null;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("Cannot answer because session is invalid");
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
			return;
		}

		UserProfile userProfile;
		try {
			UserProfileManager profileManager = session.getProfileManager();
			userProfile = profileManager.getUserProfile(messageID, false);
		} catch (GetFailedException e) {
			logger.error("Cannot get the user profile", e);
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
			return;
		}

		// find file in user profile
		Index index = userProfile.getFileById(fileKey);
		if (index == null || index.isFolder()) {
			logger.info("File not found in the user profile, cannot return a chunk");
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
			return;
		}

		// check if file is on disk
		Path path = FileUtil.getPath(session.getRoot(), index);
		if (path == null || !path.toFile().exists()) {
			logger.info("File not found on disk, cannot return a chunk");
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
			return;
		}

		Chunk chunk = null;
		try {
			// retrieve the requested file part (offset and length)
			chunk = FileChunkUtil.getChunk(path.toFile(), chunkLength, chunkNumber, "chunk-" + chunkNumber);
		} catch (IOException e) {
			logger.error("Cannot read the chunk", e);
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
			return;
		}

		// verify the chunk hash
		byte[] md5Hash = HashUtil.hash(chunk.getData());
		if (HashUtil.compare(md5Hash, chunkHash)) {
			logger.debug("MD5 hash of the chunk {} has been verified, returning the chunk", chunkNumber);

			// return the content of the file part
			sendDirectResponse(createResponse(new ChunkMessageResponse(chunk)));
		} else {
			logger.warn("MD5 hash of the read chunk {} and of the expected file does not match", chunkNumber);
			sendDirectResponse(createResponse(new ChunkMessageResponse(AnswerType.DECLINED)));
		}
	}
}
