package org.hive2hive.core.process.context;

import org.hive2hive.core.model.MetaDocument;

public interface IGetMetaContext {

	void setMetaDocument(MetaDocument metaDocument);

	MetaDocument getMetaDocument();
}
