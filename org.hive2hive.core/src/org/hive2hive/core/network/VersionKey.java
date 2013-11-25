package org.hive2hive.core.network;

import java.nio.ByteBuffer;

import net.tomp2p.peers.Number160;

/**
 * The version key (160bit) is split into two parts: The timestamp (64bit) and the hash of the
 * previous version (96bit). We can verify if the put is valid if the previous version is the latest
 * one (with the highest timestamp).
 * 
 * @author Nico
 * 
 */
// TODO this class is never used. @ippes: delete it?
@Deprecated
public class VersionKey {
	private final byte[] timestamp = new byte[8];
	private final byte[] previousHash = new byte[12];
	private final Number160 versionKey;

	public VersionKey(Number160 versionKey) {
		this.versionKey = versionKey;
		versionKey.toByteArray(timestamp, 0);
		versionKey.toByteArray(previousHash, timestamp.length);
	}

	// TODO should have only 96 bits
	public Number160 getPreviousHash() {
		return new Number160(previousHash);
	}

	public long getTimestamp() {
		return bytesToLong(timestamp);
	}

	/* Source: http://stackoverflow.com/a/4485196 */
	private long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(bytes);
		buffer.flip(); // need flip
		return buffer.getLong();
	}

	public Number160 getVersionKey() {
		return versionKey;
	}
}