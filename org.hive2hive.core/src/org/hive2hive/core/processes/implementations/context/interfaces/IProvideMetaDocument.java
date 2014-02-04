package org.hive2hive.core.processes.implementations.context.interfaces;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;

public interface IProvideMetaDocument {

	void provideMetaDocument(MetaDocument metaDocument);

	void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument);
}
