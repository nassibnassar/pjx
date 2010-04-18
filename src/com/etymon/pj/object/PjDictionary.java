package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of the PDF dictionary type.
   @author Nassib Nassar
*/
public class PjDictionary
	extends PjObject {

	/**
	   Creates an empty dictionary.
	*/
	public PjDictionary() {
		_h = new Hashtable();
	}

	/**
	   Creates a dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjDictionary(Hashtable h) {
		_h = h;
	}

	/**
	   Returns the Hashtable used to represent this dictionary.
	   @return the Hashtable used to represent this dictionary.
	*/
	public Hashtable getHashtable() {
		return _h;
	}

	/**
	   Writes this dictionary to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public long writePdf(OutputStream os) throws IOException {
		long z = writeln(os, "<<");
		PjName name;
		for (Enumeration enum = _h.keys();
		     enum.hasMoreElements();) {
			name = ((PjName)(enum.nextElement()));
			z = z + name.writePdf(os);
			z = z + write(os, " ");
			z = z + ((PjObject)(_h.get(name))).writePdf(os);
			z = z + writeln(os, "");
		}
		z = z + write(os, ">>");
		return z;
	}

	/**
	   Returns a string representation of this dictionary in PDF format.
	   @return the string representation.
	public String toString() {
		StringBuffer sb = new StringBuffer("<<");
		sb.append(PjConst.PDF_EOL);
		PjName name;
		for (Enumeration enum = _h.keys();
		     enum.hasMoreElements();) {
			name = ((PjName)(enum.nextElement()));
			sb.append(name.toString());
			sb.append(" ");
			sb.append(((PjObject)(_h.get(name))).toString());
			sb.append(PjConst.PDF_EOL);
		}
		sb.append(">>");
		return sb.toString();
	}
	 */

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjDictionary(cloneHt());
	}

	protected Hashtable cloneHt() throws CloneNotSupportedException {
		Hashtable ht = new Hashtable(Math.max(_h.size(), 1));
		Object key;
		Object value;
		for (Enumeration m = _h.keys(); m.hasMoreElements();) {
			key = m.nextElement();
			value = _h.get(key);
			if (value instanceof PjObject) {
				ht.put(key, ((PjObject)value).clone());
			} else {
				throw new CloneNotSupportedException("Object in dictionary is not a PjObject.");
			}
		}
		return ht;
	}
	
	/**
	   Renumbers object references within this object.  This
	   method calls itself recursively to comprehensively renumber
	   all objects contained within this object.
	   @param map the table of object number mappings.  Each
	   object number is looked up by key in the hash table, and
	   the associated value is assigned as the new object number.
	   The map hash table should consist of PjNumber keys and
	   PjReference values.
	*/
	public void renumber(Hashtable map) {
		Object key;
		PjObject obj;
		Object r;
		for (Enumeration m = _h.keys(); m.hasMoreElements();) {
			key = m.nextElement();
			try {
				obj = (PjObject)(_h.get(key));
				if (obj instanceof PjReference) {
					r = map.get(((PjReference)obj).getObjNumber());
					if (r != null) {
						_h.put(key, r);
					}
				} else {
					obj.renumber(map);
				}
			}
			catch (ClassCastException e) {
				// ignore bad objects
			}
		}
	}

	protected PjObject hget(PjName name) throws InvalidPdfObjectException {
		Object obj = _h.get(name);
		if (obj != null) {
			if (obj instanceof PjObject) {
				return (PjObject)obj;
			} else {
				throw new InvalidPdfObjectException(name.getString() + " is not a PDF object.");
			}
		} else {
			return null;
		}
	}
	
	protected PjReference hgetReference(PjName name) throws InvalidPdfObjectException {
		Object obj = _h.get(name);
		if (obj != null) {
			if (obj instanceof PjReference) {
				return (PjReference)obj;
			} else {
				throw new InvalidPdfObjectException(name.getString() + " is not a reference.");
			}
		} else {
			return null;
		}
	}
	
	protected Hashtable _h;
	
}
