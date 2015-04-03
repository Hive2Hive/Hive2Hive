package org.hive2hive.core.serializer;

import io.netty.buffer.ByteBuf;

public class SerializerUtil {

	/**
	 * Converts a {@link ByteBuf} to a byte array without changing the buffer's reader index.
	 * Copied from http://stackoverflow.com/a/19309312
	 * 
	 * @param buffer the buffer to read the byte from
	 * @return a byte array (or empty array if the buffer cannot be read)
	 */
	public static byte[] convertToByteArray(ByteBuf buffer) {
		byte[] rawData = new byte[buffer.readableBytes()];
		int readerIndex = buffer.readerIndex();
		buffer.getBytes(readerIndex, rawData);
		return rawData;
	}
}
