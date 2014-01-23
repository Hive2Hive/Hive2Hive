package org.hive2hive.core.process.list;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Simple process to fetch the digest (files/folders currently stored in the network).
 * 
 * @author Seppi
 */
public class GetFileListProcess extends Process implements IGetFileListProcess {

	private final GetFileListContext context;

	public GetFileListProcess(NetworkManager networkManager) throws NoSessionException {
		super(networkManager);
		if (networkManager.getSession() == null) {
			throw new NoSessionException();
		}

		context = new GetFileListContext(this);

		setNextStep(new GetFileListProcessStep());
	}

	@Override
	public List<Path> getFiles() throws IllegalProcessStateException {
		List<Path> digest = context.getDigest();
		if (digest == null)
			throw new IllegalProcessStateException("The process is not done yet or has been stopped");
		else
			return digest;
	}

	@Override
	public GetFileListContext getContext() {
		return context;
	}

}
