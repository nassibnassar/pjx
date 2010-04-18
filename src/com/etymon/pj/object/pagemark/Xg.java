package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: g.
   @author Nassib Nassar
*/
public class Xg
	extends PageMarkOperator {

	public Xg(PjNumber gray) {
		_gray = gray;
	}

	public PjNumber getGray() {
		return _gray;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _gray.writePdf(os);
		z = z + writeln(os, " g");
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
		if (obj instanceof Xg) {
			return (_gray.equals(((Xg)obj)._gray));
		} else {
			return false;
		}
	}

	private PjNumber _gray;
	
}
