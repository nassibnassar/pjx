package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tm.
   @author Nassib Nassar
*/
public class XTm
	extends PageMarkOperator {

	public XTm(PjNumber a, PjNumber b, PjNumber c, PjNumber d, PjNumber x, PjNumber y) {
		_a = a;
		_b = b;
		_c = c;
		_d = d;
		_x = x;
		_y = y;
	}

	public PjNumber getA() {
		return _a;
	}

	public PjNumber getB() {
		return _b;
	}

	public PjNumber getC() {
		return _c;
	}

	public PjNumber getD() {
		return _d;
	}

	public PjNumber getX() {
		return _x;
	}

	public PjNumber getY() {
		return _y;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _a.writePdf(os);
		z = z + write(os, ' ');
		z = z + _b.writePdf(os);
		z = z + write(os, ' ');
		z = z + _c.writePdf(os);
		z = z + write(os, ' ');
		z = z + _d.writePdf(os);
		z = z + write(os, ' ');
		z = z + _x.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y.writePdf(os);
		z = z + writeln(os, " Tm");
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
		if (obj instanceof XTm) {
			return ( (_a.equals(((XTm)obj)._a)) &&
				 (_b.equals(((XTm)obj)._b)) &&
				 (_c.equals(((XTm)obj)._c)) &&
				 (_d.equals(((XTm)obj)._d)) &&
				 (_x.equals(((XTm)obj)._x)) &&
				 (_y.equals(((XTm)obj)._y)) );
		} else {
			return false;
		}
	}

	private PjNumber _a;
	private PjNumber _b;
	private PjNumber _c;
	private PjNumber _d;
	private PjNumber _x;
	private PjNumber _y;
	
}
