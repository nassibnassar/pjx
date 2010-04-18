package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: BMC.
   @author Nassib Nassar
*/
public class XBMC
	extends PageMarkOperator {

	public XBMC(PjName tag) {
		_tag = tag;
	}

	public PjName getTag() {
		return _tag;
	}
	
	public long writePdf(OutputStream os) throws IOException {
		long z = _tag.writePdf(os);
		z = z + write(os, " BMC ");
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
		if (obj instanceof XBMC) {
			return (_tag.equals(((XBMC)obj)._tag));
		} else {
			return false;
		}
	}

	private PjName _tag;
	
}
