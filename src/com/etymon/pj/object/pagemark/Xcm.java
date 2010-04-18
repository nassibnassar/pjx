package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics state operator: cm.
   @author Nassib Nassar
*/
public class Xcm
	extends PageMarkOperator {

	public Xcm(PjNumber a, PjNumber b, PjNumber c, PjNumber d, PjNumber e, PjNumber f) {
		_a = a;
		_b = b;
		_c = c;
		_d = d;
		_e = e;
		_f = f;
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

	public PjNumber getE() {
		return _e;
	}

	public PjNumber getF() {
		return _f;
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
		z = z + _e.writePdf(os);
		z = z + write(os, ' ');
		z = z + _f.writePdf(os);
		z = z + writeln(os, " cm");
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
		if (obj instanceof Xcm) {
			return ( (_a.equals(((Xcm)obj)._a)) &&
				 (_b.equals(((Xcm)obj)._b)) &&
				 (_c.equals(((Xcm)obj)._c)) &&
				 (_d.equals(((Xcm)obj)._d)) &&
				 (_e.equals(((Xcm)obj)._e)) &&
				 (_f.equals(((Xcm)obj)._f)) );
		} else {
			return false;
		}
	}

	private PjNumber _a;
	private PjNumber _b;
	private PjNumber _c;
	private PjNumber _d;
	private PjNumber _e;
	private PjNumber _f;
	
}
