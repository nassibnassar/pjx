package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tj.
   @author Nassib Nassar
*/
public class XTj
	extends PageMarkOperator {

	public XTj(PjString text) {
		_text = text;
	}

	public PjString getText() {
		return _text;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _text.writePdf(os);
		z = z + writeln(os, " Tj");
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
		if (obj instanceof XTj) {
			return (_text.equals(((XTj)obj)._text));
		} else {
			return false;
		}
	}

	private PjString _text;
	
}
