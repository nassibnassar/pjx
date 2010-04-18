package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tf.
   @author Nassib Nassar
*/
public class XTf
	extends PageMarkOperator {

	public XTf(PjName name, PjNumber size) {
		_name = name;
		_size = size;
	}

	public PjName getName() {
		return _name;
	}
	
	public PjNumber getSize() {
		return _size;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _name.writePdf(os);
		z = z + write(os, ' ');
		z = z + _size.writePdf(os);
		z = z + writeln(os, " Tf");
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
		if (obj instanceof XTf) {
			return ( (_name.equals(((XTf)obj)._name)) &&
				 (_size.equals(((XTf)obj)._size)) );
		} else {
			return false;
		}
	}

	private PjName _name;
	private PjNumber _size;
	
}
