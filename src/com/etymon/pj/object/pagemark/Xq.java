package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Graphics operator: q.
   @author Nassib Nassar
*/
public class Xq
	extends PageMarkOperator {

	public Xq() {
	}

	public long writePdf(OutputStream os) throws IOException {
		return writeln(os, "q");
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
		return (obj instanceof Xq);
	}

}
