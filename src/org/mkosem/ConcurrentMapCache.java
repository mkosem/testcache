package org.mkosem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class ConcurrentMapCache<K,V> implements ICache<K,V> {
	private final Map<K,V> cache_;
	
	public ConcurrentMapCache(int concurrencyLevel, int initialCapacity, Float fillFactor) {
		cache_ = new ConcurrentHashMap<K, V>(initialCapacity, fillFactor, concurrencyLevel);
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
		
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}
	
	@Override
	public void destroy(){
		cache_.clear();
	}
}
