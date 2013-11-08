package org.hive2hive.core.process.common.put;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;

/**
 * Puts a {@link MetaFile} or a {@link MetaFolder} object into the DHT after encrypting it with the given key.
 * 
 * @author Nico
 * 
 */
public class PutMetaDocumentStep extends PutProcessStep {

	public PutMetaDocumentStep(MetaDocument metaDocument, ProcessStep nextStep) {
		super(metaDocument.getId().toString(), H2HConstants.META_DOCUMENT, null, nextStep);
	}

	@Override
	public void start() {
		// TODO encrypt the meta document with its public key
		// then put it.
	}

}
