package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: ".
   @author Nassib Nassar
*/
public class Xquot
	extends PageMarkOperator {

	public Xquot(PjNumber aw, PjNumber ac, PjString text) {
		_aw = aw;
		_ac = ac;
		_text = text;
	}

	public PjNumber getAW() {
		return _aw;
	}

	public PjNumber getAC() {
		return _ac;
	}

	public PjString getText() {
		return _text;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _aw.writePdf(os);
		z = z + write(os, ' ');
		z = z + _ac.writePdf(os);
		z = z + write(os, ' ');
		z = z + _text.writePdf(os);
		z = z + writeln(os, " \"");
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
		if (obj instanceof Xquot) {
			return ( (_aw.equals(((Xquot)obj)._aw)) &&
				 (_ac.equals(((Xquot)obj)._ac)) &&
				 (_text.equals(((Xquot)obj)._text)) );
		} else {
			return false;
		}
	}

	private PjNumber _aw;
	private PjNumber _ac;
	private PjString _text;
	
}
