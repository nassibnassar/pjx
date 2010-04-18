package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF type 1 font dictionary.
   @author Nassib Nassar
*/
public class PjFontType1
	extends PjFont {

	/**
	  Creates a new type 1 font dictionary.
	*/
	public PjFontType1() {
		super();
		_h.put(PjName.SUBTYPE, PjName.TYPE1);
	}

	/**
	   Creates a type 1 font dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjFontType1(Hashtable h) {
		super(h);
	}

	/**
	   Examines a dictionary to see if it is a PDF type 1 font.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjFontType1 object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		Hashtable h = dictionary.getHashtable();
		// check if the Type is Font and Subtype is Type1
		try {
			PjName type = (PjName)(h.get(PjName.TYPE));
			if (type == null) {
				return false;
			}
			if ( ! type.equals(PjName.FONT) ) {
				return false;
			}
			PjName subtype = (PjName)(h.get(PjName.SUBTYPE));
			if (subtype == null) {
				return false;
			}
			if ( ! subtype.equals(PjName.TYPE1) ) {
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
		return new PjFontType1(cloneHt());
	}
		
}
