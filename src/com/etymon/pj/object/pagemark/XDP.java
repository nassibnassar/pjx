package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Marked content operator: DP.
   @author Nassib Nassar
*/
public class XDP
	extends PageMarkOperator {

	public XDP(PjName tag, PjDictionary properties) {
		_tag = tag;
		_properties = properties;
	}

	public XDP(PjName tag, PjName properties) {
		_tag = tag;
		_properties = properties;
	}

	public PjName getTag() {
		return _tag;
	}
	
	public PjObject getProperties() {
		return _properties;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _tag.writePdf(os);
		z = z + write(os, ' ');
		z = z + _properties.writePdf(os);
		z = z + writeln(os, " DP");
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
		if (obj instanceof XDP) {
			return ( (_tag.equals(((XDP)obj)._tag)) &&
				 (_properties.equals(((XDP)obj)._properties)) );
		} else {
			return false;
		}
	}

	private PjName _tag;
	private PjObject _properties;
	
}
