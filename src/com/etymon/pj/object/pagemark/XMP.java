package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: MP.
   @author Nassib Nassar
*/
public class XMP
	extends PageMarkOperator {

	public XMP(PjName tag) {
		_tag = tag;
	}

	public PjName getTag() {
		return _tag;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _tag.writePdf(os);
		z = z + writeln(os, " MP");
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
		if (obj instanceof XMP) {
			return (_tag.equals(((XMP)obj)._tag));
		} else {
			return false;
		}
	}

	private PjName _tag;
	
}
