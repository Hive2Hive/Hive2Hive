package org.hive2hive.core.network.data.futures;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.network.data.IGetListener;
import org.hive2hive.core.network.data.NetworkContent;

public class FutureGetListener extends BaseFutureAdapter<FutureGet> {

	protected final String locationKey;
	protected final String contentKey;
	protected final Number160 versionKey;
	protected final IGetListener listener;

	public FutureGetListener(String locationKey, String contentKey, Number160 versionKey,
			IGetListener listener) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.versionKey = versionKey;
		this.listener = listener;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (future.isFailed() || future.getData() == null) {
			if (listener != null)
				listener.handleGetResult(null);
		} else {
			if (listener != null)
				listener.handleGetResult((NetworkContent) future.getData().object());
		}
	}

}
