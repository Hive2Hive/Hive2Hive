package org.hive2hive.core.client;

import org.hive2hive.core.H2HNode;

public class SessionInstance {

	private H2HNode node;
	
	public void setH2HNode(H2HNode node){
		this.node = node;
	}
	
	public H2HNode getH2HNode(){
		return node;
	}
	
}
