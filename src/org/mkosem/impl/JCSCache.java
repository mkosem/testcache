package org.mkosem.impl;

import java.io.Serializable;
import java.util.Properties;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
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
public class JCSCache<K extends Serializable, V extends Serializable> implements ICache<K, V> {
	private CacheAccess<K, V> cache_ = null;

	public JCSCache(int capacity, int concurrencyLevel) throws CacheException {
		JCS.setConfigProperties(new Properties());
		CompositeCacheAttributes attributes = new CompositeCacheAttributes();
		attributes.setMaxObjects(capacity);
		attributes.setUseDisk(false);
		attributes.setUseRemote(false);
		attributes.setUseLateral(false);
		cache_ = JCS.getInstance("test", attributes);
	}

	@Override
	public void destroy() {
		try {
			cache_.clear();
			cache_.dispose();
			CompositeCacheManager.getInstance().shutDown();
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}

	@Override
	public String getDescription() {
		return "JCS";
	}

	@Override
	public void put(K key, V value) {
		try {
			cache_.put(key, value);
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}
}