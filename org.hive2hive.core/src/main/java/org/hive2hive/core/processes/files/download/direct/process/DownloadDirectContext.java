package org.hive2hive.core.processes.files.download.direct.process;

import java.io.File;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;

public class DownloadDirectContext {

	private final DownloadTaskDirect task;
	private final MetaChunk metaChunk;
	private final File tempDestination;

	private PeerAddress selectedPeer;
	private String userName;

	public DownloadDirectContext(DownloadTaskDirect task, MetaChunk metaChunk, File tempDestination) {
		this.task = task;
		this.metaChunk = metaChunk;
		this.tempDestination = tempDestination;
	}

	public DownloadTaskDirect getTask() {
		return task;
	}

	public MetaChunk getMetaChunk() {
		return metaChunk;
	}

	public File getTempDestination() {
		return tempDestination;
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
