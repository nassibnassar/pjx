package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color operator: K.
   @author Nassib Nassar
*/
public class XXK
	extends PageMarkOperator {

	public XXK(PjNumber c, PjNumber m, PjNumber y, PjNumber k) {
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
		z = z + writeln(os, " K");
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
		if (obj instanceof XXK) {
			return ( (_c.equals(((XXK)obj)._c)) &&
				 (_m.equals(((XXK)obj)._m)) &&
				 (_y.equals(((XXK)obj)._y)) &&
				 (_k.equals(((XXK)obj)._k)) );
		} else {
			return false;
		}
	}

	private PjNumber _c;
	private PjNumber _m;
	private PjNumber _y;
	private PjNumber _k;
	
}
