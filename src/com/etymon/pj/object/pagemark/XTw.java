package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Text operator: Tw.
   @author Nassib Nassar
*/
public class XTw
	extends PageMarkOperator {

	public XTw(PjNumber wordSpace) {
		_wordSpace = wordSpace;
	}

	public PjNumber getWordSpace() {
		return _wordSpace;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = _wordSpace.writePdf(os);
		z = z + writeln(os, " Tw");
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
		if (obj instanceof XTw) {
			return (_wordSpace.equals(((XTw)obj)._wordSpace));
		} else {
			return false;
		}
	}

	private PjNumber _wordSpace;
	
}
