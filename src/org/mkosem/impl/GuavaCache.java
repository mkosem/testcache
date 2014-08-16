package org.mkosem.impl;

import org.mkosem.ICache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
public class GuavaCache<K, V> implements ICache<K, V> {
	private final Cache<K, V> cache_;

	public GuavaCache(int capacity, int concurrencyLevel) {
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
	public String getDescription() {
		return "Guava CacheBuilder";
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
	}
}