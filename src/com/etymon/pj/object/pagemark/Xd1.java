package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Type 3 font operator: d1.
   @author Nassib Nassar
*/
public class Xd1
	extends PageMarkOperator {

	public Xd1(PjNumber wx, PjNumber wy, PjNumber llx, PjNumber lly, PjNumber urx, PjNumber ury) {
		_wx = wx;
		_wy = wy;
		_llx = llx;
		_lly = lly;
		_urx = urx;
		_ury = ury;
	}

	public PjNumber getWX() {
		return _wx;
	}

	public PjNumber getWY() {
		return _wy;
	}

	public PjNumber getLLX() {
		return _llx;
	}

	public PjNumber getLLY() {
		return _lly;
	}

	public PjNumber getURX() {
		return _urx;
	}

	public PjNumber getURY() {
		return _ury;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _wx.writePdf(os);
		z = z + write(os, ' ');
		z = z + _wy.writePdf(os);
		z = z + write(os, ' ');
		z = z + _llx.writePdf(os);
		z = z + write(os, ' ');
		z = z + _lly.writePdf(os);
		z = z + write(os, ' ');
		z = z + _urx.writePdf(os);
		z = z + write(os, ' ');
		z = z + _ury.writePdf(os);
		z = z + writeln(os, " d1");
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
		if (obj instanceof Xd1) {
			return ( (_wx.equals(((Xd1)obj)._wx)) &&
				 (_wy.equals(((Xd1)obj)._wy)) &&
				 (_llx.equals(((Xd1)obj)._llx)) &&
				 (_lly.equals(((Xd1)obj)._lly)) &&
				 (_urx.equals(((Xd1)obj)._urx)) &&
				 (_ury.equals(((Xd1)obj)._ury)) );
		} else {
			return false;
		}
	}

	private PjNumber _wx;
	private PjNumber _wy;
	private PjNumber _llx;
	private PjNumber _lly;
	private PjNumber _urx;
	private PjNumber _ury;
	
}
