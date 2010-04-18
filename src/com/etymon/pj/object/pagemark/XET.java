package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: ET.
   @author Nassib Nassar
*/
public class XET
	extends PageMarkOperator {

	public XET() {
	}

	public long writePdf(OutputStream os) throws IOException {
		return writeln(os, "ET");
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
		return (obj instanceof XET);
	}

}
