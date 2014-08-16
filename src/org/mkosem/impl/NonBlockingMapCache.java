package org.mkosem.impl;

import java.util.Map;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
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
public class NonBlockingMapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;

	public NonBlockingMapCache(int capacity, int concurrencyLevel) {
		cache_ = new NonBlockingHashMap<K, V>(capacity);
	}

	@Override
	public void destroy() {
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}

	@Override
	public String getDescription() {
		return "Non-Blocking Map";
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);

	}
}