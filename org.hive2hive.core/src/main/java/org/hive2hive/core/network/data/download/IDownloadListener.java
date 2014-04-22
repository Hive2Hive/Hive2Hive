package org.hive2hive.core.network.data.download;

public interface IDownloadListener {

	void downloadFinished(BaseDownloadTask task);

	void downloadFailed(BaseDownloadTask task, String reason);
}
