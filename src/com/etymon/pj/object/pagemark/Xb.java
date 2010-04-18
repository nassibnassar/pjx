package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Path operator: b.
   @author Nassib Nassar
*/
public class Xb
	extends PageMarkOperator {

	public Xb() {
	}

	public long writePdf(OutputStream os) throws IOException {
		return writeln(os, "b");
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
		return (obj instanceof Xb);
	}

}
