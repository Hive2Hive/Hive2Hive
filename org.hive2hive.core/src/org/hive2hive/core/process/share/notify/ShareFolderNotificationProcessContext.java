package org.hive2hive.core.process.share.notify;

import java.security.PublicKey;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class ShareFolderNotificationProcessContext extends ProcessContext implements IGetMetaContext {

	private final PublicKey folderKey;
	private MetaDocument metaDocument;

	public ShareFolderNotificationProcessContext(Process process, PublicKey folderKey) {
		super(process);
		this.folderKey = folderKey;
	}

	public PublicKey getFolderKey() {
		return folderKey;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}
}
