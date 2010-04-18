package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: M.
   @author Nassib Nassar
*/
public class XXM
	extends PageMarkOperator {

	public XXM(PjNumber miterLimit) {
		_miterLimit = miterLimit;
	}

	public PjNumber getMiterLimit() {
		return _miterLimit;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _miterLimit.writePdf(os);
		z = z + writeln(os, " M");
		return z;
	}
	
	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	*/
	public Object clone() {
		return this;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof XXM) {
			return (_miterLimit.equals(((XXM)obj)._miterLimit));
		} else {
			return false;
		}
	}

	private PjNumber _miterLimit;
	
}
