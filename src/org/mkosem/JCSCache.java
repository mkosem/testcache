package org.mkosem;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

public class JCSCache<K extends Serializable,V extends Serializable> implements ICache<K,V> {
	private CacheAccess<K, V> cache_ = null;
	
	public JCSCache(int capacity) throws CacheException {
		JCS.setConfigProperties(new Properties());
		CompositeCacheAttributes attributes = new CompositeCacheAttributes();
		attributes.setMaxObjects(capacity);
		attributes.setUseDisk(false);
		attributes.setUseRemote(false);
		attributes.setUseLateral(false);
		cache_ = JCS.getInstance("test", attributes);
	}

	@Override
	public void put(K key, V value) {
		try {
			cache_.put(key, value);
		} catch (CacheException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public V get(K key) {
		return cache_.get(key);
	}
	
	@Override
	public void destroy(){
		cache_.dispose();
		try {
			CompositeCacheManager.getInstance().shutDown();
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}
}
