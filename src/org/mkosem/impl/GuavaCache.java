package org.mkosem.impl;

import org.mkosem.ICache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCache<K, V> implements ICache<K, V> {
	private final Cache<K, V> cache_;

	public GuavaCache(int concurrencyLevel, int capacity) {
		cache_ = CacheBuilder.newBuilder().concurrencyLevel(concurrencyLevel).maximumSize(capacity).initialCapacity(capacity).build();
	}

	@Override
	public void destroy() {
		cache_.invalidateAll();
	}

	@Override
	public V get(K key) {
		return cache_.getIfPresent(key);
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
	}
}