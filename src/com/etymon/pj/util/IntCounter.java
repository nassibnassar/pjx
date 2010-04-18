package com.etymon.pj.util;

public class IntCounter {

	public IntCounter(int c) {
		_c = c;
	}

	public int value() {
		return _c;
	}

	public void set(int value) {
		_c = value;
	}
	
	public void inc() {
		_c++;
	}
	
	public void inc(int size) {
		_c = _c + size;
	}
	
	public void dec() {
		_c--;
	}
	
	public void dec(int size) {
		_c = _c - size;
	}
	
	int _c;
	
}
