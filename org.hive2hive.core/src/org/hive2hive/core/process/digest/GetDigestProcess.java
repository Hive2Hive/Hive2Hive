package org.hive2hive.core.process.digest;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Simple process to fetch the digest (files currently stored in the network).
 * 
 * @author Seppi
 */
public class GetDigestProcess extends Process {
	
	private final GetDigestContext context;

	public GetDigestProcess(NetworkManager networkManager) {
		super(networkManager);

		context = new GetDigestContext(this);
		
		setNextStep(new GetDigestProcessStep());
	}
	
	public List<Path> getDigest(){
		return context.getDigest();
	}
	
	@Override
	public GetDigestContext getContext() {
		return context;
	}

}
