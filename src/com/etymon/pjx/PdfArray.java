/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.util.*;

/**
   Represents the PDF array object.
   @author Nassib Nassar
*/
public class PdfArray
	extends PdfObject {

	/**
	   The contents of the array.
	 */
	protected List _a;
	
	/**
	   A protected constructor intended to be called only from
	   {@link #wrap(List) wrap(List)}.
	 */
	protected PdfArray() {  // called only from wrap()
	}

	/**
	   Constructs an array object from a list of
	   <code>PdfObject</code> instances.
	   @param a the list containing the <code>PdfObject</code>
	   instances.
	 */
	public PdfArray(List a) {
		_a = Collections.unmodifiableList(new ArrayList(a));
	}

	protected PdfObject filterContents(PdfObjectFilter f) throws PdfFormatException {
		List newList = new ArrayList(_a.size());
		for (Iterator t = _a.iterator(); t.hasNext(); ) {
			Object obj = t.next();
			if ( !(obj instanceof PdfObject) ) {
				throw new PdfFormatException("Array element is not a PDF object.");
			}
			
			PdfObject objF = ((PdfObject)obj).filter(f);
			
			if (objF != null) {
				newList.add(objF);
			}
		}
		return f.postFilter(new PdfArray(newList));
	}
	
	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfArray) ) ) {
			return false;
		}

		return _a.equals( ((PdfArray)obj)._a );
	}

	/**
	   Returns the list of elements contained in this array.
	   @return the list of elements.  The returned
	   <code>List</code> object is unmodifiable.
	 */
	public List getList() {
		return _a;
	}

	public int hashCode() {
		return _a.hashCode();
	}

	/**
	   A factory for fast construction of this class.  The
	   constructed object will be a wrapper around the specified
	   <code>List</code>.  The calling method must ensure that the
	   <code>List</code> is never externally modified, in order to
	   meet the immutability requirement of {@link PdfObject
	   PdfObject}.
	   @param a the <code>List</code> to be used to back this
	   array.
	   @return the constructed object.
	 */
	protected static PdfArray wrap(List a) {
		PdfArray pa = new PdfArray();
		pa._a = Collections.unmodifiableList(a);
		return pa;
	}
	
	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {

		DataOutputStream dos = w.getDataOutputStream();

		int count = 2;  // for [ and ]
		
		dos.write('[');
		
		boolean first = true;
		for (Iterator p = _a.iterator(); p.hasNext(); ) {
			PdfObject obj = (PdfObject)p.next();
			if (first) {
				count += obj.writePdf(w, false);
				first = false;
			} else {
				count += obj.writePdf(w, true);
			}
		}
		
		dos.write(']');

		return count;
	}

}
