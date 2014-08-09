package org.mkosem;

public interface ICache<K, V> {
	public void destroy();

	public V get(K key);

	public void put(K key, V value);
}