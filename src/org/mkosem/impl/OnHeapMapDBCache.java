package org.mkosem.impl;

import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mkosem.ICache;



public class OnHeapMapDBCache<K,V> implements ICache<K,V> {
	private final HTreeMap<K,V> cache_;
	
	public OnHeapMapDBCache(int initialCapacity) {
		cache_ = DBMaker.newHeapDB().transactionDisable().make().createHashMap("cache").<K, V>make();
	}

	@Override
	public void destroy(){
		cache_.clear();
		cache_.close();
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
