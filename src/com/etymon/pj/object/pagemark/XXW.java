package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Path operator: W.
   @author Nassib Nassar
*/
public class XXW
	extends PageMarkOperator {

	public XXW() {
	}

	public long writePdf(OutputStream os) throws IOException {
		return writeln(os, "W");
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
		return (obj instanceof XXW);
	}

}
