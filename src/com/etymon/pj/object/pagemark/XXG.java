package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: G.
   @author Nassib Nassar
*/
public class XXG
	extends PageMarkOperator {

	public XXG(PjNumber gray) {
		_gray = gray;
	}

	public PjNumber getGray() {
		return _gray;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _gray.writePdf(os);
		z = z + writeln(os, " G");
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
		if (obj instanceof XXG) {
			return (_gray.equals(((XXG)obj)._gray));
		} else {
			return false;
		}
	}

	private PjNumber _gray;
	
}
