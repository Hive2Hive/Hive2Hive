package org.hive2hive.client.menu;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;

import org.apache.commons.io.FileUtils;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

public class FileMenu extends H2HConsoleMenu {

	public H2HConsoleMenuItem CreateRootDirectory;

	private File rootDirectory;

	@Override
	protected void createItems() {

		CreateRootDirectory = new H2HConsoleMenuItem("Create Root Directory") {
			protected void execute() throws Exception {

				rootDirectory = new File(FileUtils.getUserDirectory(), "H2H_" + System.currentTimeMillis());
				
				if (isExpertMode) {
					System.out.printf(
							"Please specify the root directory path or enter 'ok' if you agree with '%s'.",
							rootDirectory.toPath());

					String input = awaitStringParameter();

					if (!input.equalsIgnoreCase("ok")) {
						while (!Files.exists(new File(input).toPath(), LinkOption.NOFOLLOW_LINKS)) {

							printError("This directory does not exist. Please retry.");
							input = awaitStringParameter();
						}
					}
				}

				if (!Files.exists(rootDirectory.toPath(), LinkOption.NOFOLLOW_LINKS)) {
					try {
						FileUtils.forceMkdir(rootDirectory);
					} catch (Exception e) {
						printError(String.format("Exception on creating the root directory %s: " + e,
								rootDirectory.toPath()));
					}
				}
			}
		};

	}

	@Override
	protected void addMenuItems() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getInstruction() {
		// TODO Auto-generated method stub
		return null;
	}

}
