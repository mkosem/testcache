package org.mkosem.impl;

import java.util.Map;
import org.mkosem.ICache;
import com.google.common.collect.MapMaker;

public class GuavaConcurrentMapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;

	public GuavaConcurrentMapCache(int concurrencyLevel, int initialCapacity) {
		cache_ = new MapMaker().concurrencyLevel(concurrencyLevel).initialCapacity(initialCapacity).makeMap();
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