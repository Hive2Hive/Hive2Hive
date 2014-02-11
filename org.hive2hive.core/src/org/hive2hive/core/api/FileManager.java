package org.hive2hive.core.api;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

public class FileManager extends NetworkNode implements IFileManager {

	public FileManager(INetworkConfiguration networkConfiguration) {
		super(networkConfiguration);
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
