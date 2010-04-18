package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: TL.
   @author Nassib Nassar
*/
public class XTL
	extends PageMarkOperator {

	public XTL(PjNumber leading) {
		_leading = leading;
	}

	public PjNumber getLeading() {
		return _leading;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _leading.writePdf(os);
		z = z + writeln(os, " TL");
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
		if (obj instanceof XTL) {
			return (_leading.equals(((XTL)obj)._leading));
		} else {
			return false;
		}
	}

	private PjNumber _leading;
	
}
