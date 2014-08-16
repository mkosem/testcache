package org.mkosem.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
public class ReadWriteLockMapCache<K, V> implements ICache<K, V> {
	private final Map<K, V> cache_;
	private final ReadWriteLock lock_;
	private final Lock readLock_;
	private final Lock writeLock_;

	public ReadWriteLockMapCache(int capacity, int concurrencyLevel) {
		lock_ = new ReentrantReadWriteLock();
		cache_ = new HashMap<K, V>(capacity);
		readLock_ = lock_.writeLock();
		writeLock_ = lock_.writeLock();
	}

	@Override
	public void destroy() {
		try {
			writeLock_.lock();
			cache_.clear();
		} finally {
			writeLock_.unlock();
		}
	}

	@Override
	public V get(K key) {
		try {
			readLock_.lock();
			return cache_.get(key);
		} finally {
			readLock_.unlock();
		}
	}

	@Override
	public String getDescription() {
		return "ReadWriteLock-Synchronized HashMap";
	}

	@Override
	public void put(K key, V value) {
		try {
			writeLock_.lock();
			cache_.put(key, value);
		} finally {
			writeLock_.unlock();
		}
	}
}