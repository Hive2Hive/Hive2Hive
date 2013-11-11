package org.hive2hive.core.network;

import java.security.PublicKey;
import java.util.HashMap;

public class PublicKeyCache {

	private static HashMap<String, PublicKey> cache = new HashMap<String, PublicKey>();
	
	public static synchronized PublicKey getKey(String userId){
		return cache.get(userId);
	}
	
	public static synchronized void addKey(String userId, PublicKey publicKey){
		cache.put(userId, publicKey);
	}
}
