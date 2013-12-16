package org.hive2hive.core.process.move;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class MoveFileProcessContext extends ProcessContext implements IGetMetaContext {

	private MetaDocument metaDocument;
	private final File source;
	private final File destination;
	private KeyPair nodeKeyPair;
	private KeyPair destinationParentKeys;

	public MoveFileProcessContext(Process process, File source, File destination) {
		super(process);
		this.source = source;
		this.destination = destination;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	public File getSource() {
		return source;
	}

	public File getDestination() {
		return destination;
	}

	public void setFileNodeKeys(KeyPair nodeKeyPair) {
		this.nodeKeyPair = nodeKeyPair;
	}

	public KeyPair getFileNodeKeys() {
		return nodeKeyPair;
	}

	public void setDestinationParentKeys(KeyPair destinationParentKeys) {
		this.destinationParentKeys = destinationParentKeys;
	}

	public KeyPair getDestinationParentKeys() {
		return destinationParentKeys;
	}

}
