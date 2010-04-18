package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color space operator: cs.
   @author Nassib Nassar
*/
public class Xcs
	extends PageMarkOperator {

	public Xcs(PjName colorSpace) {
		_colorSpace = colorSpace;
	}

	public PjName getColorSpace() {
		return _colorSpace;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _colorSpace.writePdf(os);
		z = z + writeln(os, " cs");
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
		if (obj instanceof Xcs) {
			return (_colorSpace.equals(((Xcs)obj)._colorSpace));
		} else {
			return false;
		}
	}

	private PjName _colorSpace;
	
}
