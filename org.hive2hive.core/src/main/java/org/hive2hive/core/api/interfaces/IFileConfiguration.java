package org.hive2hive.core.api.interfaces;

import java.math.BigInteger;

/**
 * The file configuration is essential for the performance of the system. Depending on the application, the
 * developer can adapt the parameters. Hive2Hive automatically changes its behavior for example while cleaning
 * old versions, depending on the configured version numbers.
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface IFileConfiguration {

	/**
	 * The maximum size that a file can have. The higher the size here, the heavier the network gets. More
	 * data must be sent around. File size may be large if you only plan to use Hive2Hive in a (local)
	 * High-speed network.<br>
	 * If you only need to synchronize small (meta) data, lower the file size here.
	 * 
	 * @return the number of bytes that a file can have
	 */
	BigInteger getMaxFileSize();

	/**
	 * This parameter configures the number of available versions, once a file gets updated. In case the
	 * developer does not want the version feature, just set this value to 1.<br>
	 * The more versions of a file are allowed, the heavier becomes the network.
	 * 
	 * @return the number of versions that a file may have
	 */
	int getMaxNumOfVersions();

	/**
	 * Even though this parameter would not be necessary (since {@link IFileConfiguration#getMaxFileSize()} *
	 * {@link IFileConfiguration#getMaxNumOfVersions()} would fit here), it allows to configure the system
	 * more detailed. For example uploading a large file (video) should be allowed, but you want to keep the
	 * number of versions small, you could reduce this value. Then, a large file cannot have as many versions
	 * as a small file may have.
	 * 
	 * @return the total number of bytes summed up over all versions of a specific file
	 */
	BigInteger getMaxSizeAllVersions();

	/**
	 * When uploading a file it gets chunked and distributed over the network. The network load becomes
	 * statistically a more even distribution. Adding a large file to the network without chunking it forces
	 * few peers to work very hard. We recommend to adapt the chunk size to the network performance.<br>
	 * Another aspect of chunking is that the file is more secure. An attacker that cracks (we don't hope he's
	 * able to do so) one chunk may not see the whole file.
	 * 
	 * @return the number of bytes a chunk has.
	 */
	int getChunkSize();

}
