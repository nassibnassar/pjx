package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: '.
   @author Nassib Nassar
*/
public class Xapost
	extends PageMarkOperator {

	public Xapost(PjString text) {
		_text = text;
	}

	public PjString getText() {
		return _text;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _text.writePdf(os);
		z = z + writeln(os, " '");
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
		if (obj instanceof Xapost) {
			return (_text.equals(((Xapost)obj)._text));
		} else {
			return false;
		}
	}

	private PjString _text;
	
}
