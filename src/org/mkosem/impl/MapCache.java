package org.mkosem.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class MapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;

	public MapCache(int initialCapacity, Float fillFactor) {
		cache_ = Collections.synchronizedMap(new HashMap<K, V>(initialCapacity, fillFactor));
	}

	@Override
	public void destroy() {
		cache_.clear();
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}

	@Override
	public String getDescription() {
		return "HashMap";
	}

	@Override
	public void put(K key, V value) {
		cache_.put(key, value);
	}
}