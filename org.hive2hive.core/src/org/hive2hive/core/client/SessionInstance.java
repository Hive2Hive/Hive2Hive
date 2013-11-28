package org.hive2hive.core.client;

import java.util.ArrayList;

import org.hive2hive.core.H2HNode;
import org.hive2hive.core.network.NetworkManager;

public class SessionInstance {

	private ArrayList<NetworkManager> network;
	private H2HNode node;
	
	public void setH2HNode(H2HNode node){
		this.node = node;
	}
	
	public H2HNode getH2HNode(){
		return node;
	}

	public ArrayList<NetworkManager> getNetwork() {
		return network;
	}

	public void setNetwork(ArrayList<NetworkManager> network) {
		this.network = network;
	}
	
}
