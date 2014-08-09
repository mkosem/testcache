package org.mkosem.impl;

import java.util.Map;
import org.mkosem.ICache;
import com.google.common.collect.MapMaker;

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
public class GuavaConcurrentMapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;

	public GuavaConcurrentMapCache(int concurrencyLevel, int initialCapacity) {
		cache_ = new MapMaker().concurrencyLevel(concurrencyLevel).initialCapacity(initialCapacity).makeMap();
	}

	@Override
	public void destroy() {
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