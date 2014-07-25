package org.mkosem.impl;

import java.util.Map;

import org.mkosem.ICache;

import se7.java.util.concurrent.ConcurrentHashMap;



public class SE7ConcurrentMapCache<K,V> implements ICache<K,V> {
	private final Map<K,V> cache_;
	
	public SE7ConcurrentMapCache(int concurrencyLevel, int initialCapacity, Float fillFactor) {
		cache_ = new ConcurrentHashMap<K, V>(initialCapacity, fillFactor, concurrencyLevel);
	}

	@Override
	public void destroy(){}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}
	
	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
		
	}
}
