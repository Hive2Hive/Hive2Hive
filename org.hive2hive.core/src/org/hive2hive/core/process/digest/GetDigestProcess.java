package org.hive2hive.core.process.digest;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Simple process to fetch the digest (files currently stored in the network).
 * 
 * @author Seppi
 */
public class GetDigestProcess extends Process implements IGetDigestProcess {

	private final GetDigestContext context;

	public GetDigestProcess(NetworkManager networkManager) throws NoSessionException {
		super(networkManager);
		if (networkManager.getSession() == null) {
			throw new NoSessionException();
		}

		context = new GetDigestContext(this);

		setNextStep(new GetDigestProcessStep());
	}

	@Override
	public List<Path> getDigest() throws IllegalProcessStateException {
		List<Path> digest = context.getDigest();
		if (digest == null)
			throw new IllegalProcessStateException("The process is not done yet or has been stopped");
		else
			return digest;
	}

	@Override
	public GetDigestContext getContext() {
		return context;
	}

}
