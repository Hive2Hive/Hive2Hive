package org.hive2hive.core.processes.files.recover;

import java.util.List;

import org.hive2hive.core.model.IFileVersion;

public interface IVersionSelector {

	/**
	 * Select a version of the available versions. This interface is typically implemented by the developer
	 * using the Hive2Hive library. This call can be blocking without harming the process. However, endless
	 * blocking does not allow the process to finish.
	 * 
	 * @param availableVersions all versions in the DHT
	 * @return the selected version
	 */
	IFileVersion selectVersion(List<IFileVersion> availableVersions);

	/**
	 * Set a name of the currently recovered file. Note that the name should not equal with the original name.
	 * The easiest case is to return null here, Hive2Hive automatically selects a new name.
	 * 
	 * @param fullName the original file name with extension
	 * @param name the original file name without the extension
	 * @param extension the file extension
	 * @return the name of the new file. If null is returned, the new file name is automatically chosen.
	 */
	String getRecoveredFileName(String fullName, String name, String extension);
}
