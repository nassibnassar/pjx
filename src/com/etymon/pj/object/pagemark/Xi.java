package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: i.
   @author Nassib Nassar
*/
public class Xi
	extends PageMarkOperator {

	public Xi(PjNumber flatness) {
		_flatness = flatness;
	}

	public PjNumber getFlatness() {
		return _flatness;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _flatness.writePdf(os);
		z = z + writeln(os, " i");
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
		if (obj instanceof Xi) {
			return (_flatness.equals(((Xi)obj)._flatness));
		} else {
			return false;
		}
	}

	private PjNumber _flatness;
	
}
