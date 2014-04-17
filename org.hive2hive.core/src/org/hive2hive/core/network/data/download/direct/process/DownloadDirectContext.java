package org.hive2hive.core.network.data.download.direct.process;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.data.download.direct.DownloadTaskDirect;

public class DownloadDirectContext {

	private final DownloadTaskDirect task;
	private PeerAddress selectedPeer;
	private String userName;

	public DownloadDirectContext(DownloadTaskDirect task) {
		this.task = task;
	}

	public DownloadTaskDirect getTask() {
		return task;
	}

	public void setSelectedPeer(PeerAddress selectedPeer, String userName) {
		this.userName = userName;
		this.selectedPeer = selectedPeer;
	}

	public PeerAddress getSelectedPeer() {
		return selectedPeer;
	}

	public String getUserName() {
		return userName;
	}
}
