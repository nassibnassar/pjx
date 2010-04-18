package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.object.*;

/**
   A page marking object.
   @author Nassib Nassar
*/
public abstract class PageMarkObject
	extends PageMark {

	protected Vector cloneVector() throws CloneNotSupportedException {
		Vector v = new Vector(_operators.size());
		Enumeration m = _operators.elements();
		while (m.hasMoreElements()) {
			Object value = m.nextElement();
			if (value instanceof PjObject) {
				v.addElement(((PjObject)value).clone());
			} else {
				throw new CloneNotSupportedException("Object in array is not a PjObject.");
			}
		}
		return v;
	}

	protected Vector _operators;
	
}
