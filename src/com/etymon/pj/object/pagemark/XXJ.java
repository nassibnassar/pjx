package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: J.
   @author Nassib Nassar
*/
public class XXJ
	extends PageMarkOperator {

	public XXJ(PjNumber lineCap) {
		_lineCap = lineCap;
	}

	public PjNumber getLineCap() {
		return _lineCap;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _lineCap.writePdf(os);
		z = z + writeln(os, " J");
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
		if (obj instanceof XXJ) {
			return (_lineCap.equals(((XXJ)obj)._lineCap));
		} else {
			return false;
		}
	}

	private PjNumber _lineCap;
	
}
