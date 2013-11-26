package org.hive2hive.core.network.data.futures;

import org.hive2hive.core.network.data.IGetUserMessageListener;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;

public class FutureGetUserMessageListener extends BaseFutureAdapter<FutureGet> {

	public FutureGetUserMessageListener(String locationKey, IGetUserMessageListener listener) {

	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		
	}

}
