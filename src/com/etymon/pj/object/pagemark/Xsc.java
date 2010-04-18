package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Color operator: sc.
   @author Nassib Nassar
*/
public class Xsc
	extends PageMarkOperator {

	public Xsc(PjNumber c1, PjNumber c2, PjNumber c3, PjNumber c4) {
		_c1 = c1;
		_c2 = c2;
		_c3 = c3;
		_c4 = c4;
		_opcount = 4;
	}

	public Xsc(PjNumber c1, PjNumber c2, PjNumber c3) {
		_c1 = c1;
		_c2 = c2;
		_c3 = c3;
		_opcount = 3;
	}

	public PjNumber getC1() {
		return _c1;
	}

	public PjNumber getC2() {
		return _c2;
	}

	public PjNumber getC3() {
		return _c3;
	}

	public PjNumber getC4() {
		if (_opcount == 4) {
			return _c4;
		} else {
			return null;
		}
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _c1.writePdf(os);
		z = z + write(os, ' ');
		z = z + _c2.writePdf(os);
		z = z + write(os, ' ');
		z = z + _c3.writePdf(os);
		if (_opcount == 4) {
			z = z + write(os, ' ');
			z = z + _c4.writePdf(os);
		}
		z = z + writeln(os, " sc");
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
		if (obj instanceof Xsc) {
			if (_opcount == 4) {
				return ( (_opcount == ((Xsc)obj)._opcount) &&
					 (_c1.equals(((Xsc)obj)._c1)) &&
					 (_c2.equals(((Xsc)obj)._c2)) &&
					 (_c3.equals(((Xsc)obj)._c3)) &&
					 (_c4.equals(((Xsc)obj)._c4)) );
			} else {
				return ( (_opcount == ((Xsc)obj)._opcount) &&
					 (_c1.equals(((Xsc)obj)._c1)) &&
					 (_c2.equals(((Xsc)obj)._c2)) &&
					 (_c4.equals(((Xsc)obj)._c3)) );
			}
		} else {
			return false;
		}
	}

	private PjNumber _c1;
	private PjNumber _c2;
	private PjNumber _c3;
	private PjNumber _c4;
	private int _opcount;
	
}
