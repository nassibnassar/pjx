package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tc.
   @author Nassib Nassar
*/
public class XTc
	extends PageMarkOperator {

	public XTc(PjNumber charSpace) {
		_charSpace = charSpace;
	}

	public PjNumber getCharSpace() {
		return _charSpace;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _charSpace.writePdf(os);
		z = z + writeln(os, " Tc");
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
		if (obj instanceof XTc) {
			return (_charSpace.equals(((XTc)obj)._charSpace));
		} else {
			return false;
		}
	}

	private PjNumber _charSpace;
	
}
