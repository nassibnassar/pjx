package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: w.
   @author Nassib Nassar
*/
public class Xw
	extends PageMarkOperator {

	public Xw(PjNumber lineWidth) {
		_lineWidth = lineWidth;
	}

	public PjNumber getLineWidth() {
		return _lineWidth;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _lineWidth.writePdf(os);
		z = z + writeln(os, " w");
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
		if (obj instanceof Xw) {
			return (_lineWidth.equals(((Xw)obj)._lineWidth));
		} else {
			return false;
		}
	}

	private PjNumber _lineWidth;
	
}
