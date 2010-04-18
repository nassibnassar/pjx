package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: d.
   @author Nassib Nassar
*/
public class Xd
	extends PageMarkOperator {

	public Xd(PjArray array, PjNumber phase) {
		_array = array;
		_phase = phase;
	}

	public PjArray getArray() {
		return _array;
	}
	
	public PjNumber getPhase() {
		return _phase;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _array.writePdf(os);
		z = z + write(os, ' ');
		z = z + _phase.writePdf(os);
		z = z + writeln(os, " d");
		return z;
	}
	
	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
           @exception CloneNotSupportedException if the instance can not be cloned.
	*/
        public Object clone() throws CloneNotSupportedException {
		return new Xd((PjArray)(_array.clone()), _phase);
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Xd) {
			return ( (_array.equals(((Xd)obj)._array)) &&
				 (_phase.equals(((Xd)obj)._phase)) );
		} else {
			return false;
		}
	}

	private PjArray _array;
	private PjNumber _phase;
	
}
