package org.hive2hive.core.client;

public class NetworkMenu extends ConsoleMenu {

	public NetworkMenu(H2HConsole console) {
		super(console);
	}

	@Override
	protected void addMenuHandlers() {

		add("Set MaxFileSize", new IConsoleMenuCallback() {
			public void invoke() {
  
			}
		});

		add("Set MaxNumOfVersions", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set MaxSizeAllVersions", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set ChunkSize", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set AutostartProcesses", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set IsMasterPeer", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set BootstrapAddress", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});

		add("Set RootPath", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});
	}

	@Override
	public String getInstruction() {
		return "Please select a network configuration option.";
	}

}
