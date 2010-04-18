package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF Pages dictionary.
   @author Nassib Nassar
*/
public class PjPages
	extends PjPagesNode {

	/**
	   Creates a new Pages dictionary.
	*/
	public PjPages() {
		super();
		_h.put(PjName.TYPE, PjName.PAGES);
	}

	/**
	   Creates a Pages dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjPages(Hashtable h) {
		super(h);
	}

	public void setKids(PjArray kids) {
		_h.put(PjName.KIDS, kids);
	}

	public void setKids(PjReference kids) {
		_h.put(PjName.KIDS, kids);
	}

	public PjObject getKids() throws InvalidPdfObjectException {
		return hget(PjName.KIDS);
	}

	public void setCount(PjNumber count) {
		_h.put(PjName.COUNT, count);
	}

	public void setCount(PjReference count) {
		_h.put(PjName.COUNT, count);
	}

	public PjObject getCount() throws InvalidPdfObjectException {
		return hget(PjName.COUNT);
	}

	/**
	   Examines a dictionary to see if it is a PDF Pages object.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjPages object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		Hashtable h = dictionary.getHashtable();
		// check if the Type is Pages
		try {
			PjName type = (PjName)(h.get(PjName.TYPE));
			if (type == null) {
				return false;
			}
			if ( ! type.equals(PjName.PAGES) ) {
				return false;
			}
		}
		catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjPages(cloneHt());
	}
	
}
