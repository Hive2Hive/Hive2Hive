package org.hive2hive.core.encryption;

public final class EncryptedContent {

	private final String content;
	private final String initVector;
	
	public EncryptedContent(String content, String initVector){
		this.content = content;
		this.initVector = initVector;
	}
	
	public final String getContent(){
		return content;
	}
	
	public final String getInitVector(){
		return initVector;
	}
}
