package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Td.
   @author Nassib Nassar
*/
public class XTd
	extends PageMarkOperator {

	public XTd(PjNumber x, PjNumber y) {
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
		z = z + writeln(os, " Td");
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
		if (obj instanceof XTd) {
			return ( (_x.equals(((XTd)obj)._x)) &&
				 (_y.equals(((XTd)obj)._y)) );
		} else {
			return false;
		}
	}

	private PjNumber _x;
	private PjNumber _y;
	
}
