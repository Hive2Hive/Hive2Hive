package org.hive2hive.processes.implementations.context;

import java.io.File;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;

public class UpdateFileProcessContext extends AddFileProcessContext implements IProvideMetaDocument {

	public UpdateFileProcessContext(File file, boolean inRoot, H2HSession session) {
		super(file, inRoot, session);
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		provideNewMetaDocument(metaDocument);
	}
}
