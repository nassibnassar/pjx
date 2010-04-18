package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   XObject operator: Do.
   @author Nassib Nassar
*/
public class XDo
	extends PageMarkOperator {

	public XDo(PjName xobject) {
		_xobject = xobject;
	}

	public PjName getXObject() {
		return _xobject;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _xobject.writePdf(os);
		z = z + writeln(os, " Do");
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
		if (obj instanceof XDo) {
			return (_xobject.equals(((XDo)obj)._xobject));
		} else {
			return false;
		}
	}

	private PjName _xobject;
	
}
