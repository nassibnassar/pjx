package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color operator: rg.
   @author Nassib Nassar
*/
public class Xrg
	extends PageMarkOperator {

	public Xrg(PjNumber r, PjNumber g, PjNumber b) {
		_r = r;
		_g = g;
		_b = b;
	}

	public PjNumber getR() {
		return _r;
	}

	public PjNumber getG() {
		return _g;
	}

	public PjNumber getB() {
		return _b;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _r.writePdf(os);
		z = z + write(os, ' ');
		z = z + _g.writePdf(os);
		z = z + write(os, ' ');
		z = z + _b.writePdf(os);
		z = z + writeln(os, " rg");
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
		if (obj instanceof Xrg) {
			return ( (_r.equals(((Xrg)obj)._r)) &&
				 (_g.equals(((Xrg)obj)._g)) &&
				 (_b.equals(((Xrg)obj)._b)) );
		} else {
			return false;
		}
	}

	private PjNumber _r;
	private PjNumber _g;
	private PjNumber _b;
	
}
