package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF font encoding dictionary.
   @author Nassib Nassar
*/
public class PjEncoding
	extends PjDictionary {

	/**
	   Creates a new encoding dictionary.
	*/
	public PjEncoding() {
		super();
                _h.put(PjName.TYPE, PjName.ENCODING);
	}

	/**
	   Creates an encoding dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjEncoding(Hashtable h) {
		super(h);
	}

	public void setBaseEncoding(PjName baseEncoding) {
		_h.put(PjName.BASEENCODING, baseEncoding);
	}

	public void setBaseEncoding(PjReference baseEncoding) {
		_h.put(PjName.BASEENCODING, baseEncoding);
	}

	public PjObject getBaseEncoding() throws InvalidPdfObjectException {
		return hget(PjName.BASEENCODING);
	}

	public void setDifferences(PjArray differences) {
		_h.put(PjName.DIFFERENCES, differences);
	}

	public void setDifferences(PjReference differences) {
		_h.put(PjName.DIFFERENCES, differences);
	}

	public PjObject getDifferences() throws InvalidPdfObjectException {
		return hget(PjName.DIFFERENCES);
	}

	/**
	   Examines a dictionary to see if it is a PDF font encoding
	   dictionary.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjEncoding object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
                Hashtable h = dictionary.getHashtable();
                // check if the Type is Encoding
                try {
                        PjName type = (PjName)(h.get(PjName.TYPE));
                        if (type == null) {
                                return false;
                        }
                        if ( ! type.equals(PjName.ENCODING) ) {
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
		return new PjEncoding(cloneHt());
	}
	
}
