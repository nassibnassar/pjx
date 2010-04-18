package com.etymon.pj.util;

import java.util.*;
import com.etymon.pj.object.*;

public class PjObjectVector {

	public PjObjectVector() {
		_v = new Vector();
		_free = 1;
	}

	public PjObjectVector(int initialCapacity) {
		_v = new Vector(initialCapacity);
		_free = 1;
	}

	public int getFirstFree() {
		synchronized (this) {
			return _free;
		}
	}
	
	public PjObject objectAt(int index) {
		synchronized (this) {
			if (index >= _v.size()) {
				return null;
			} else {
				return (PjObject)(_v.elementAt(index));
			}
		}
	}
	
	public void setObjectAt(PjObject obj, int index) {
		synchronized (this) {
			if (index >= _v.size()) {
				_v.setSize(index + 1);
			}
			_v.setElementAt(obj, index);
			if (index == _free) {
				_free = findFirstFree(index + 1);
			}
		}
	}
	
	public int size() {
		synchronized (this) {
			return _v.size();
		}
	}

	
	protected int findFirstFree(int start) {
		synchronized (this) {
			int x = start;
			int size = _v.size();
			while ( (x < size) && (_v.elementAt(x) != null) ) {
				x++;
			}
			return x;
		}
	}

	protected Vector _v;
	protected int _free;
	
}
