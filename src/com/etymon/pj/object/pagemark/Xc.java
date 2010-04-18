package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Path operator: c.
   @author Nassib Nassar
*/
public class Xc
	extends PageMarkOperator {

	public Xc(PjNumber x1, PjNumber y1, PjNumber x2, PjNumber y2, PjNumber x3, PjNumber y3) {
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
		_x3 = x3;
		_y3 = y3;
	}

	public PjNumber getX1() {
		return _x1;
	}

	public PjNumber getY1() {
		return _y1;
	}

	public PjNumber getX2() {
		return _x2;
	}

	public PjNumber getY2() {
		return _y2;
	}

	public PjNumber getX3() {
		return _x3;
	}

	public PjNumber getY3() {
		return _y3;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _x1.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y1.writePdf(os);
		z = z + write(os, ' ');
		z = z + _x2.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y2.writePdf(os);
		z = z + write(os, ' ');
		z = z + _x3.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y3.writePdf(os);
		z = z + writeln(os, " c");
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
		if (obj instanceof Xc) {
			return ( (_x1.equals(((Xc)obj)._x1)) &&
				 (_y1.equals(((Xc)obj)._y1)) &&
				 (_x2.equals(((Xc)obj)._x2)) &&
				 (_y2.equals(((Xc)obj)._y2)) &&
				 (_x3.equals(((Xc)obj)._x3)) &&
				 (_y3.equals(((Xc)obj)._y3)) );
		} else {
			return false;
		}
	}

	private PjNumber _x1;
	private PjNumber _y1;
	private PjNumber _x2;
	private PjNumber _y2;
	private PjNumber _x3;
	private PjNumber _y3;
	
}
