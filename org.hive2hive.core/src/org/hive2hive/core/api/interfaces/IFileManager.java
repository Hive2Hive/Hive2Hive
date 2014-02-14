package org.hive2hive.core.api.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;

/**
 * Basic interface for all file operations.
 * 
 * @author Christian
 * 
 */
public interface IFileManager extends IManager {

	IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException;

	IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException;

	IProcessComponent move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException;

	IProcessComponent delete(File file) throws NoSessionException, NoPeerConnectionException;

	IProcessComponent recover(File file, IVersionSelector versionSelector) throws FileNotFoundException,
			IllegalArgumentException, NoSessionException, NoPeerConnectionException;

	IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException;

	IResultProcessComponent<List<Path>> getFileList();

}