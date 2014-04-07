package org.hive2hive.core.processes.implementations.files.recover;

import java.util.List;

import org.hive2hive.core.model.T;

public interface IVersionSelector {

	/**
	 * Select a version of the available versions. This interface is typically implemented by the developer
	 * using the Hive2Hive library. This call can be blocking without harming the process. However, endless
	 * blocking does not allow the process to finish.
	 * 
	 * @param availableVersions all versions in the DHT
	 * @return the selected version
	 */
	T selectVersion(List<T> availableVersions);
}
