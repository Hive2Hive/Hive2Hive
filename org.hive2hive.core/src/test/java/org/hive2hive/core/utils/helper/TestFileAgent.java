package org.hive2hive.core.utils.helper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.utils.FileTestUtil;

public class TestFileAgent implements IFileAgent {

	private final File root;

	public TestFileAgent() {
		root = FileTestUtil.getTempDirectory();
	}

	public TestFileAgent(File root) {
		this.root = root;
	}

	@Override
	public void writeCache(String name, byte[] data) throws IOException {
		FileUtils.writeByteArrayToFile(new File(root, name), data);
	}

	@Override
	public byte[] readCache(String name) throws IOException {
		return FileUtils.readFileToByteArray(new File(root, name));
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	protected void finalize() throws Throwable {
		// cleanup when object is not used anymore
		FileUtils.deleteDirectory(root);
	}
}
