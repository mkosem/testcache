package org.mkosem.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.mkosem.ICache;

/*
 * Copyright 2014 Matt Kosem
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Ehcache<K, V> implements ICache<K, V> {
	private final String cacheName = "test";
	private final Cache cache_;

	public Ehcache(int capacity, int concurrencyLevel) {
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
	public String getDescription() {
		return "EHCache";
	}

	@Override
	public void put(K key, V value) {
		Element element = new Element(key, value);
		cache_.put(element);
	}
}