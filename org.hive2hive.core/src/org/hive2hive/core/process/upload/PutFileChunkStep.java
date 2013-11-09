package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutProcessStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Puts a chunk and recursively calls itself until all chunks are stored in the DHT.
 * Afterwards, it continues with putting the meta file object in the DHT.
 * 
 * @author Nico
 * 
 */
public class PutFileChunkStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutFileChunkStep.class);

	private final File file;
	private final long offset;
	private final List<KeyPair> chunkKeys;

	public PutFileChunkStep(File file, long offset, List<KeyPair> chunkKeys) {
		// the details are set later
		super(null, H2HConstants.FILE_CHUNK, null, null);
		this.file = file;
		this.offset = offset;
		this.chunkKeys = chunkKeys;
	}

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		byte[] data = new byte[context.getConfig().getChunkSize()];
		int read = -1;
		try {
			// read the next chunk of the file considering the offset
			RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
			rndAccessFile.seek(offset);
			read = rndAccessFile.read(data);
			rndAccessFile.close();
		} catch (IOException e) {
			logger.error("Could not read the file", e);
			getProcess().stop(e.getMessage());
			return;
		}

		if (read > 0) {
			// create a chunk
			KeyPair chunkKey = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
			Chunk chunk = new Chunk(chunkKey.getPublic(), data, chunkKeys.size(), read);
			chunkKeys.add(chunkKey);

			// more data to read (increase offset)
			nextStep = new PutFileChunkStep(file, offset + data.length, chunkKeys);

			try {
				// encrypt the chunk prior to put such that nobody can read it
				HybridEncryptedContent encryptedContent = H2HEncryptionUtil.encryptDES(chunk,
						chunkKey.getPublic(), AES_KEYLENGTH.BIT_256);

				// start the put and continue with next chunk
				logger.debug("Uploading chunk " + chunk.getOrder() + " of file " + file.getAbsolutePath());
				put(chunk.getId().toString(), H2HConstants.FILE_CHUNK, encryptedContent);
			} catch (DataLengthException | InvalidKeyException | IllegalStateException
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Could not encrypt the chunk", e);
				getProcess().stop(e.getMessage());
			}
		} else {
			logger.debug("All chunks uploaded. Continue with meta data.");

			// nothing read, stop putting chunks and start next step
			// put the meta folder and update the user profile
			UserProfile profile = context.getUserProfile();

			AddFileToUserProfileStep updateProfileStep = new AddFileToUserProfileStep(file, profile,
					context.getCredentials());

			// TODO check if file already existed. If not, create new Meta file. If yes, create new version in
			// existing meta file
			MetaFile newMetaFile = createNewMetaFile();
			PutMetaDocumentStep putMetaFolder = new PutMetaDocumentStep(newMetaFile, updateProfileStep);
			getProcess().setNextStep(putMetaFolder);
		}
	}

	private MetaFile createNewMetaFile() {
		KeyPair fileKey = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		MetaFile metaFile = new MetaFile(fileKey.getPublic());
		FileVersion version = new FileVersion(0, getFileSize(), System.currentTimeMillis());
		version.setChunkIds(chunkKeys);

		List<FileVersion> versions = new ArrayList<FileVersion>(1);
		versions.add(version);
		metaFile.setVersions(versions);

		return metaFile;
	}

	/**
	 * Note that file.length can be very slowly (see
	 * http://stackoverflow.com/questions/116574/java-get-file-size-efficiently)
	 * 
	 * @return the file size in bytes
	 * @throws IOException
	 */
	private long getFileSize() {
		try {
			FileInputStream stream = new FileInputStream(file);
			long size = stream.getChannel().size();
			stream.close();
			return size;
		} catch (IOException e) {
			return file.length();
		}
	}
}
