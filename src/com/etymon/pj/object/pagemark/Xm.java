package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Path operator: m.
   @author Nassib Nassar
*/
public class Xm
	extends PageMarkOperator {

	public Xm(PjNumber x, PjNumber y) {
		_x = x;
		_y = y;
	}

	public PjNumber getX() {
		return _x;
	}

	public PjNumber getY() {
		return _y;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _x.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y.writePdf(os);
		z = z + writeln(os, " m");
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
		if (obj instanceof Xm) {
			return ( (_x.equals(((Xm)obj)._x)) &&
				 (_y.equals(((Xm)obj)._y)) );
		} else {
			return false;
		}
	}

	private PjNumber _x;
	private PjNumber _y;
	
}
