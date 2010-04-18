package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: j.
   @author Nassib Nassar
*/
public class Xj
	extends PageMarkOperator {

	public Xj(PjNumber lineJoin) {
		_lineJoin = lineJoin;
	}

	public PjNumber getLineJoin() {
		return _lineJoin;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _lineJoin.writePdf(os);
		z = z + writeln(os, " j");
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
		if (obj instanceof Xj) {
			return (_lineJoin.equals(((Xj)obj)._lineJoin));
		} else {
			return false;
		}
	}

	private PjNumber _lineJoin;
	
}
