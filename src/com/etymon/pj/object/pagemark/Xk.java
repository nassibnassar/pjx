package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color operator: k.
   @author Nassib Nassar
*/
public class Xk
	extends PageMarkOperator {

	public Xk(PjNumber c, PjNumber m, PjNumber y, PjNumber k) {
		_c = c;
		_m = m;
		_y = y;
		_k = k;
	}

	public PjNumber getC() {
		return _c;
	}

	public PjNumber getM() {
		return _m;
	}

	public PjNumber getY() {
		return _y;
	}

	public PjNumber getK() {
		return _k;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _c.writePdf(os);
		z = z + write(os, ' ');
		z = z + _m.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y.writePdf(os);
		z = z + write(os, ' ');
		z = z + _k.writePdf(os);
		z = z + writeln(os, " k");
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
		if (obj instanceof Xk) {
			return ( (_c.equals(((Xk)obj)._c)) &&
				 (_m.equals(((Xk)obj)._m)) &&
				 (_y.equals(((Xk)obj)._y)) &&
				 (_k.equals(((Xk)obj)._k)) );
		} else {
			return false;
		}
	}

	private PjNumber _c;
	private PjNumber _m;
	private PjNumber _y;
	private PjNumber _k;
	
}
