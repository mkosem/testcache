package org.mkosem;

public class TestElement {
	private final String key_;
	private final ValueBox value_;

	public TestElement(String key, ValueBox value) {
		key_ = key;
		value_ = value;
	}

	public String getKey() {
		return key_;
	}

	public ValueBox getValue() {
		return value_;
	}
}