package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color operator: RG.
   @author Nassib Nassar
*/
public class XXRG
	extends PageMarkOperator {

	public XXRG(PjNumber r, PjNumber g, PjNumber b) {
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
		z = z + writeln(os, " RG");
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
		if (obj instanceof XXRG) {
			return ( (_r.equals(((XXRG)obj)._r)) &&
				 (_g.equals(((XXRG)obj)._g)) &&
				 (_b.equals(((XXRG)obj)._b)) );
		} else {
			return false;
		}
	}

	private PjNumber _r;
	private PjNumber _g;
	private PjNumber _b;
	
}
