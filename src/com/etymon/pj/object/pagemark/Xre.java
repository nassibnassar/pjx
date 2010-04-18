package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Path operator: re.
   @author Nassib Nassar
*/
public class Xre
	extends PageMarkOperator {

	public Xre(PjNumber x, PjNumber y, PjNumber width, PjNumber height) {
		_x = x;
		_y = y;
		_width = width;
		_height = height;
	}

	public PjNumber getX() {
		return _x;
	}

	public PjNumber getY() {
		return _y;
	}

	public PjNumber getWidth() {
		return _width;
	}

	public PjNumber getHeight() {
		return _height;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _x.writePdf(os);
		z = z + write(os, ' ');
		z = z + _y.writePdf(os);
		z = z + write(os, ' ');
		z = z + _width.writePdf(os);
		z = z + write(os, ' ');
		z = z + _height.writePdf(os);
		z = z + writeln(os, " re");
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
		if (obj instanceof Xre) {
			return ( (_x.equals(((Xre)obj)._x)) &&
				 (_y.equals(((Xre)obj)._y)) &&
				 (_width.equals(((Xre)obj)._width)) &&
				 (_height.equals(((Xre)obj)._height)) );
		} else {
			return false;
		}
	}

	private PjNumber _x;
	private PjNumber _y;
	private PjNumber _width;
	private PjNumber _height;
	
}
