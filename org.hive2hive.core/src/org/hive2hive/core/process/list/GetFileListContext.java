package org.hive2hive.core.process.list;

import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.ProcessContext;

/**
 * Simple context to pass the fetched digest of currently stored files.
 * 
 * @author Seppi
 */
public class GetFileListContext extends ProcessContext {

	private List<Path> digest;

	public GetFileListContext(Process process) {
		super(process);
	}

	public List<Path> getDigest() {
		return digest;
	}

	public void setDigest(List<Path> digest) {
		this.digest = digest;
	}
}
