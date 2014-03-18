package org.mkosem;

public interface ICache<K,V> {
	public void put(K key, V value);
	public V get(K key);
}
