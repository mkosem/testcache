package org.mkosem;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCache<K,V> implements ICache<K,V> {
	private final Cache<K,V> cache_;
	
	public GuavaCache(int concurrencyLevel, int initialCapacity, Float fillFactor) {
		cache_ = CacheBuilder.newBuilder().concurrencyLevel(concurrencyLevel).initialCapacity(initialCapacity).expireAfterWrite(86400, TimeUnit.SECONDS).build();
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);	
	}

	@Override
	public V get(K key) {
		return cache_.getIfPresent(key);
	}
}
