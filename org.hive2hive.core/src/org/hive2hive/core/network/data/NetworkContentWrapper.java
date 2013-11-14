package org.hive2hive.core.network.data;

public class NetworkContentWrapper<T> extends NetworkContent {

	private static final long serialVersionUID = -3011847530347265233L;
	
	private T content;
	
	public NetworkContentWrapper(T content) {
		this.content = content;
	}
	
	public T getContent() {
		return content;
	}
	
	@Override
	public int getTimeToLive() {
		// TODO Auto-generated method stub
		return 0;
	}

}
