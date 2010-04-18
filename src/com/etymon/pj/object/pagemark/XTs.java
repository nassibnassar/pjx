package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Ts.
   @author Nassib Nassar
*/
public class XTs
	extends PageMarkOperator {

	public XTs(PjNumber rise) {
		_rise = rise;
	}

	public PjNumber getRise() {
		return _rise;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _rise.writePdf(os);
		z = z + writeln(os, " Ts");
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
		if (obj instanceof XTs) {
			return (_rise.equals(((XTs)obj)._rise));
		} else {
			return false;
		}
	}

	private PjNumber _rise;
	
}
