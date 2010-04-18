package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tr.
   @author Nassib Nassar
*/
public class XTr
	extends PageMarkOperator {

	public XTr(PjNumber render) {
		_render = render;
	}

	public PjNumber getRender() {
		return _render;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _render.writePdf(os);
		z = z + writeln(os, " Tr");
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
		if (obj instanceof XTr) {
			return (_render.equals(((XTr)obj)._render));
		} else {
			return false;
		}
	}

	private PjNumber _render;
	
}
