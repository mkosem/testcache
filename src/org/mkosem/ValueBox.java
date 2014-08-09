package org.mkosem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ValueBox implements Serializable {
	private static final long serialVersionUID = 838907810902225940L;
	private final byte[] contents_;

	ValueBox(int size) {
		contents_ = new byte[size];
		ThreadLocalRandom.current().nextBytes(contents_);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueBox other = (ValueBox) obj;
		if (!Arrays.equals(contents_, other.contents_))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(contents_);
		return result;
	}
}