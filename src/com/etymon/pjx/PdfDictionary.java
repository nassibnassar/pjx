/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.util.*;

/**
   Represents the PDF dictionary object.
   @author Nassib Nassar
*/
public class PdfDictionary
	extends PdfObject {

	/**
	   The contents of the dictionary.
	*/
	protected Map _m;

	/**
	   A protected constructor intended to be called only from
	   {@link #wrap(Map) wrap(Map)}.
	 */
	protected PdfDictionary() {
	}

	/**
	   Constructs a dictionary object from a map of
	   <code>PdfName</code> keys and <code>PdfObject</code>
	   values.
	   @param m the map containing the keys and values.
	 */
	public PdfDictionary(Map m) {
		_m = Collections.unmodifiableMap(new HashMap(m));
	}

	protected PdfObject filterContents(PdfObjectFilter f) throws PdfFormatException {
		Map newMap = new HashMap(_m.size());
		for (Iterator t = _m.keySet().iterator(); t.hasNext(); ) {
			
			Object obj = t.next();
			if ( !(obj instanceof PdfObject) ) {
				throw new PdfFormatException("Dictionary key is not a PDF object.");
			}
			PdfObject key = (PdfObject)obj;
			PdfObject keyF = key.filter(f);
			
			if (keyF != null) {
				obj = _m.get(key);
				if ( !(obj instanceof PdfObject) ) {
					throw new PdfFormatException("Dictionary value is not a PDF object.");
				}
				PdfObject valueF = ((PdfObject)obj).filter(f);
				
				if (valueF != null) {
					newMap.put(key, ((PdfObject)obj).filter(f));
				}
			}
			
		}
		return f.postFilter(new PdfDictionary(newMap));
	}
	
	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfDictionary) ) ) {
			return false;
		}

		return _m.equals( ((PdfDictionary)obj)._m );
	}

	/**
	   Returns the map of keys and values contained in this
	   dictionary.
	   @return the map of keys and values.  The returned
	   <code>Map</code> object is unmodifiable.
	 */
	public Map getMap() {
		return _m;
	}

	public int hashCode() {
		return _m.hashCode();
	}

	/**
	   A factory for fast construction of this class.  The
	   constructed object will be a wrapper around the specified
	   <code>Map</code>.  The calling method must ensure that the
	   <code>Map</code> is never externally modified, in order to
	   meet the immutability requirement of {@link PdfObject
	   PdfObject}.
	   @param m the <code>Map</code> to be used to back this
	   dictionary.
	   @return the constructed object.
	 */
	protected static PdfDictionary wrap(Map m) {
		PdfDictionary pd = new PdfDictionary();
		pd._m = Collections.unmodifiableMap(m);
		return pd;
	}
	
	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {

		DataOutputStream dos = w.getDataOutputStream();

		int count = 4;  // for << and >>
		
		dos.writeBytes("<<");

		boolean first = true;
		for (Iterator p = _m.keySet().iterator(); p.hasNext(); ) {
			PdfName key = (PdfName)p.next();
			PdfObject value = (PdfObject)_m.get(key);
			if (first) {
				count += key.writePdf(w, false);
				first = false;
			} else {
				count += key.writePdf(w, true);
			}
			count += value.writePdf(w, true);
		}
		
		dos.writeBytes(">>");

		return count;
		
	}

}
