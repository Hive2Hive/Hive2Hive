package org.hive2hive.core.process.recover;

import java.io.File;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class RecoverFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final IVersionSelector versionSelector;
	private final File file;
	private MetaDocument metaDocument;
	private FileVersion version;

	public RecoverFileProcessContext(Process process, File file, IVersionSelector versionSelector) {
		super(process);
		this.file = file;
		this.versionSelector = versionSelector;
	}

	public IVersionSelector getVersionSelector() {
		return versionSelector;
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	public void setSelectedFileVersion(FileVersion version) {
		this.version = version;
	}

	public FileVersion getSelectedFileVersion() {
		return version;
	}

	public File getFile() {
		return file;
	}
}
