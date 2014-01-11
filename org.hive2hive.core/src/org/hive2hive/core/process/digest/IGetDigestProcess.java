package org.hive2hive.core.process.digest;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.process.IProcess;

/**
 * Public interface for the {@link IGetDigestProcess} to get the digest of all files.
 * 
 * @author Nico
 * 
 */
public interface IGetDigestProcess extends IProcess {

	/**
	 * Get the list of all files that are in sync (in the DHT and on disk)
	 * 
	 * @return a list of paths pointing to the files on disk (depends on the specified root path during login)
	 * @throws IllegalProcessStateException when the method is called before the process has finished or when
	 *             it has stopped for another reason
	 */
	List<Path> getDigest() throws IllegalProcessStateException;
}
