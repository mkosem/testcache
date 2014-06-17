package org.mkosem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SemaphoreLockedMapCache<K,V> implements ICache<K,V> {
	private final Map<K,V> cache_;
	private final int size_;
	private final Semaphore lock_;
	
	public SemaphoreLockedMapCache(int initialCapacity, Float fillFactor, int numThreads) {
		size_ = numThreads;
		lock_ = new Semaphore(size_);
		cache_ = new HashMap<K,V>(initialCapacity, fillFactor);
	}

	@Override
	public void destroy(){
		try {
			lock_.acquireUninterruptibly();
			cache_.clear();
		} finally {
			lock_.release();
		}
	}

	@Override
	public V get(K key) {
		try {
			lock_.acquireUninterruptibly();
			return cache_.get(key);
		} finally {
			lock_.release();
		}
	}
	
	@Override
	public void put(K key, V value) {
		try {
			lock_.acquireUninterruptibly(size_);
			cache_.put(key, value);
		} finally {
			lock_.release(size_);
		}
	}
	
}
