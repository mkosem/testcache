package org.mkosem.impl;

import org.mkosem.ICache;
import com.hv.nitroCache.CacheEviction;

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
public class NitroCache<K, V> implements ICache<K, V> {
	private final com.hv.nitroCache.NitroCache<K, V> cache_;

	public NitroCache(int capacity, int concurrencyLevel) {
		cache_ = com.hv.nitroCache.NitroCache.getInstance(capacity, CacheEviction.LRU);
	}

	@Override
	public void destroy() {
		cache_.clear();
		cache_.shutdown();
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}

	@Override
	public String getDescription() {
		return "NitroCache (LRU)";
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
	}
}