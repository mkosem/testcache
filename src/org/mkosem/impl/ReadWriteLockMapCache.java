package org.mkosem.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mkosem.ICache;

public class ReadWriteLockMapCache<K,V> implements ICache<K,V> {
	private final Map<K,V> cache_;
	private final ReadWriteLock lock_;
	private final Lock readLock_;
	private final Lock writeLock_;
	
	public ReadWriteLockMapCache(int initialCapacity, Float fillFactor, int numThreads) {
		lock_ = new ReentrantReadWriteLock();
		cache_ = new HashMap<K,V>(initialCapacity, fillFactor);
		readLock_ = lock_.writeLock();
		writeLock_ = lock_.writeLock();
	}

	@Override
	public void destroy(){
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
	public void put(K key, V value) {
		try {
			writeLock_.lock();
			cache_.put(key, value);
		} finally {
			writeLock_.unlock();
		}
	}
	
}
