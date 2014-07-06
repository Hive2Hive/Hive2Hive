package org.hive2hive.core.processes.files.list;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Gives information about a file in the DHT
 * 
 * @author Nico
 * 
 */
public class FileTaste {

	private final Path path;
	private final File file;
	private final byte[] md5;
	private final Set<UserPermission> userPermissions;

	FileTaste(File file, Path path, byte[] md5, Set<UserPermission> userPermissions) {
		this.file = file;
		this.path = path;
		this.md5 = md5;
		this.userPermissions = userPermissions;
	}

	/**
	 * The name of the file
	 * 
	 * @return the filename
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Returns the relative path of the file to the root
	 * 
	 * @return
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Returns the absolute file with respect to the currently logged in user
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns whether the file is a file and not a directory. This is just the reverse option of
	 * {@link FileTaste#isFolder()}.
	 * 
	 * @return true when the file is a file and not a folder.
	 */
	public boolean isFile() {
		if (file.exists()) {
			return file.isFile();
		} else {
			return md5 != null;
		}
	}

	/**
	 * Returns whether the file is a directory. This is just the reverse option of {@link FileTaste#isFile()}.
	 * 
	 * @return true when the file is a folder.
	 */
	public boolean isFolder() {
		return !isFile();
	}

	/**
	 * The MD5 hash of the file. In case of a folder, this is null.
	 * 
	 * @return the MD5 hash of the newest file version
	 */
	public byte[] getMd5() {
		return md5;
	}

	/**
	 * A list of users that have permissions to this file (including the user itself)
	 * 
	 * @return the users having (any) permission to this file
	 */
	public Set<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	/**
	 * Indicates whether this file is shared with someone
	 * 
	 * @return true when the file is shared
	 */
	public boolean isShared() {
		return getUserPermissions().size() > 1;
	}

	@Override
	public String toString() {
		return String.format("%s: %s [%s] %s", isFile() ? "File" : "Folder", getPath(), getUserPermissions(),
				isFile() ? String.format("(MD5: %s)", EncryptionUtil.byteToHex(getMd5())) : "");
	}
}
