package org.hive2hive.core.model;

import java.math.BigInteger;

/**
 * Public interface for a file version
 * 
 * @author Nico
 * 
 */
public interface IFileVersion {

	/**
	 * Each version has a unique index which counts up with each version
	 * 
	 * @return the index of the version
	 */
	int getIndex();

	/**
	 * A version has a size (number of bytes)
	 * 
	 * @return file size in bytes
	 */
	BigInteger getSize();

	/**
	 * A version as a date when the version has been uploaded to the DHT.
	 * 
	 * @return the date in milliseconds of the official java date
	 */
	long getDate();
}
