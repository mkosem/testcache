package org.mkosem;

import com.hv.nitroCache.CacheEviction;

public class NitroCache<K,V> implements ICache<K,V> {
	private final com.hv.nitroCache.NitroCache<K,V> cache_;
	
	public NitroCache(int capacity) {
		cache_ = com.hv.nitroCache.NitroCache.getInstance(capacity, CacheEviction.FIFO);
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);	
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}
}
