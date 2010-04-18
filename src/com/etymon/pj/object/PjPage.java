package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF page dictionary.
   @author Nassib Nassar
*/
public class PjPage
	extends PjPagesNode {

	/**
	   Creates a new page dictionary.
	*/
	public PjPage() {
		super();
		_h.put(PjName.TYPE, PjName.PAGE);
	}

	/**
	   Creates a page dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjPage(Hashtable h) {
		super(h);
	}

	public void setContents(PjArray contents) {
		_h.put(PjName.CONTENTS, contents);
	}

	public void setContents(PjReference contents) {
		_h.put(PjName.CONTENTS, contents);
	}

	public PjObject getContents() throws InvalidPdfObjectException {
		return hget(PjName.CONTENTS);
	}

	public void setThumb(PjReference thumb) {
		_h.put(PjName.THUMB, thumb);
	}

	public PjReference getThumb() throws InvalidPdfObjectException {
		return hgetReference(PjName.THUMB);
	}

	public void setAnnots(PjArray annots) {
		_h.put(PjName.ANNOTS, annots);
	}

	public void setAnnots(PjReference annots) {
		_h.put(PjName.ANNOTS, annots);
	}

	public PjObject getAnnots() throws InvalidPdfObjectException {
		return hget(PjName.ANNOTS);
	}

	public void setB(PjArray b) {
		_h.put(PjName.B, b);
	}

	public void setB(PjReference b) {
		_h.put(PjName.B, b);
	}

	public PjObject getB() throws InvalidPdfObjectException {
		return hget(PjName.B);
	}

	/**
	   Examines a dictionary to see if it is a PDF page.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjPage object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		Hashtable h = dictionary.getHashtable();
		// check if the Type is Page
		try {
			PjName type = (PjName)(h.get(PjName.TYPE));
			if (type == null) {
				return false;
			}
			if ( ! type.equals(PjName.PAGE) ) {
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
		return new PjPage(cloneHt());
	}
	
}
