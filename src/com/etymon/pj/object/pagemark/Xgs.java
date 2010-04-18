package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: gs.
   @author Nassib Nassar
*/
public class Xgs
	extends PageMarkOperator {

	public Xgs(PjName name) {
		_name = name;
	}

	public PjName getName() {
		return _name;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _name.writePdf(os);
		z = z + writeln(os, " gs");
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
		if (obj instanceof Xgs) {
			return (_name.equals(((Xgs)obj)._name));
		} else {
			return false;
		}
	}

	private PjName _name;
	
}
