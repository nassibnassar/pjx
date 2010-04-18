package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tz.
   @author Nassib Nassar
*/
public class XTz
	extends PageMarkOperator {

	public XTz(PjNumber scale) {
		_scale = scale;
	}

	public PjNumber getScale() {
		return _scale;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _scale.writePdf(os);
		z = z + writeln(os, " Tz");
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
		if (obj instanceof XTz) {
			return (_scale.equals(((XTz)obj)._scale));
		} else {
			return false;
		}
	}

	private PjNumber _scale;
	
}
