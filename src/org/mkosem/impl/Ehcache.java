package org.mkosem.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.mkosem.ICache;

public class Ehcache<K, V> implements ICache<K, V> {
	private final String cacheName = "test";
	private final Cache cache_;

	public Ehcache(int capacity) {
		CacheConfiguration config = new CacheConfiguration();
		config.setName(cacheName);
		config.setMaxEntriesLocalHeap(capacity);
		cache_ = new Cache(config);
		CacheManager.getInstance().addCache(cache_);
	}

	@Override
	public void destroy() {
		cache_.removeAll();
		CacheManager.getInstance().removeCache(cacheName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) {
		return (V) cache_.get(key).getObjectValue();
	}

	@Override
	public void put(K key, V value) {
		Element element = new Element(key, value);
		cache_.put(element);
	}
}