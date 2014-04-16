package org.hive2hive.core.network.data.download;

public interface IDownloadListener {

	void downloadFinished(DownloadTask task);

	void downloadFailed(DownloadTask task, String reason);
}
