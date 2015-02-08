package org.hive2hive.core.network.data.vdht;

import java.util.Map;
import java.util.TreeMap;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;

public class Cache<V> extends TreeMap<Number160, V> {

	private static final long serialVersionUID = 7731754953812711346L;

	public Cache() {
		super();
	}

	public Cache(Cache<V> cache) {
		super(cache);
	}

	@Override
	public void putAll(Map<? extends Number160, ? extends V> map) {
		super.putAll(map);
		cleanUp();
	}

	public V put(Number160 key, V value) {
		try {
			return super.put(key, value);
		} finally {
			cleanUp();
		}
	}

	private void cleanUp() {
		if (!isEmpty()) {
			while (firstKey().timestamp() + H2HConstants.MAX_VERSIONS_HISTORY <= lastKey().timestamp()) {
				pollFirstEntry();
			}
		}
	}
}
