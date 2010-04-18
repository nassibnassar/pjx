package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Type 3 font operator: d0.
   @author Nassib Nassar
*/
public class Xd0
	extends PageMarkOperator {

	public Xd0(PjNumber wX, PjNumber wY) {
		_wX = wX;
		_wY = wY;
	}

	public PjNumber getWX() {
		return _wX;
	}

	public PjNumber getWY() {
		return _wY;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _wX.writePdf(os);
		z = z + write(os, ' ');
		z = z + _wY.writePdf(os);
		z = z + writeln(os, " d0");
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
		if (obj instanceof Xd0) {
			return ( (_wX.equals(((Xd0)obj)._wX)) &&
				 (_wY.equals(((Xd0)obj)._wY)) );
		} else {
			return false;
		}
	}

	private PjNumber _wX;
	private PjNumber _wY;
	
}
