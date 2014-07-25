package org.mkosem.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mkosem.ICache;

public class MapCache<K,V> implements ICache<K,V> {
	private final Map<K,V> cache_;
	
	public MapCache(int initialCapacity, Float fillFactor) {
		cache_ = Collections.synchronizedMap(new HashMap<K,V>(initialCapacity, fillFactor));
	}

	@Override
	public void destroy(){
		cache_.clear();
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
