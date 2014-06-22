package org.hive2hive.client.menu;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

public class UtilMenu extends ConsoleMenu {

	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Show Local Network Interfaces & IP Addresses") {

			@Override
			protected void execute() throws Exception {

				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface netInterface = (NetworkInterface) interfaces.nextElement();
					print(String.format("* %s: ", netInterface.getDisplayName()));

					Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress address = (InetAddress) addresses.nextElement();
						print(String.format("\t- %s", address.getHostAddress()));
					}
				}

				exit();
			}
		});

		// TODO add menu item for showing external IP
		// add(new H2HConsoleMenuItem("Show External IP") {
		//
		// @Override
		// protected void execute() throws Exception {
		//
		// URL ipService = new URL("http://automation.whatismyip.com/n09230945.asp");
		// BufferedReader in = new BufferedReader(new InputStreamReader(ipService.openStream()));
		//
		// String ip = in.readLine();
		// System.out.printf("External IP: %s", ip);
		// }
		// });
	}

	@Override
	protected String getInstruction() {
		return "Please select a util option:";
	}

}
