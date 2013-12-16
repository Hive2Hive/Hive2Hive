package org.hive2hive.client.menu;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.hive2hive.client.menuitem.H2HConsoleMenuItem;

public class UtilMenu extends ConsoleMenu {

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Show Local Network Interfaces & IP Addresses") {
			
			@Override
			protected void execute() throws Exception {
				
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	            while(interfaces.hasMoreElements())
	            {
	                NetworkInterface netInterface = (NetworkInterface) interfaces.nextElement();
	                System.out.printf("%s:\n", netInterface.getDisplayName());
	                
	                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
	                while(addresses.hasMoreElements())
	                {
	                    InetAddress address = (InetAddress) addresses.nextElement();
	                    System.out.printf("\t%s\n", address.getHostAddress());
	                }
	            }
				
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Please select a util option:\n";
	}

}
