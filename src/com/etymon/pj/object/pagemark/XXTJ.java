package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: TJ.
   @author Nassib Nassar
*/
public class XXTJ
	extends PageMarkOperator {

	public XXTJ(PjArray array) {
		_array = array;
	}

	public PjArray getArray() {
		return _array;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _array.writePdf(os);
		z = z + writeln(os, " TJ");
		return z;
	}
	
	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
           @exception CloneNotSupportedException if the instance can not be cloned.
	*/
        public Object clone() throws CloneNotSupportedException {
		return new XXTJ((PjArray)(_array.clone()));
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof XXTJ) {
			return (_array.equals(((XXTJ)obj)._array));
		} else {
			return false;
		}
	}

	private PjArray _array;
	
}
