package org.mkosem.impl;

import java.util.Map;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.mkosem.ICache;

public class NonBlockingMapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;

	public NonBlockingMapCache(int initialCapacity) {
		cache_ = new NonBlockingHashMap<K, V>(initialCapacity);
	}

	@Override
	public void destroy() {
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);

	}
}