/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   The abstract superclass of all basic PDF object types.  It is a
   requirement that any classes that extend this class be immutable.
   @author Nassib Nassar
*/
public abstract class PdfObject
	implements Cloneable {

	/**
	   Filter this object recursively through
	   <code>PdfObjectFilter</code>.  The filtered object is
	   returned.
	   @param filter the <code>PdfObjectFilter</code> instance.
	   @return the new filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject filter(PdfObjectFilter f) throws PdfFormatException {
		PdfObject obj = f.preFilter(this);
		if (obj == null) {
			return f.postFilter(null);
		} else {
			return obj.filterContents(f);
		}
	}

	/**
	   Second stage filtering, called by {@link
	   #filter(PdfObjectFilter) filter(PdfObjectFilter)}.  This is
	   only called if {@link PdfObjectFilter#preFilter(PdfObject)
	   PdfObjectFilter.preFilter(PdfObject)} did not return
	   <code>null</code>.
	   @param filter the <code>PdfObjectFilter</code> instance.
	   @return the new filtered object.
	   @throws PdfFormatException
	 */
	protected PdfObject filterContents(PdfObjectFilter f) throws PdfFormatException {
		return f.postFilter(this);
	}
	
	/**
	   Returns a shallow copy of this instance.
	   @return a clone of this instance.
	 */
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;  // this should never happen
		}
	}

	/**
	   Compares this instance with another PDF object for equality.
	   @param obj the object to compare this instance with.
	   @return <code>true</code> if the PDF objects are equal.
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	/**
	   Returns a hash code for this object.
	   @return the hash code.
	 */
	public int hashCode() {
		return super.hashCode();
	}
	
	/**
	   Returns a string representation of this instance in PDF
	   format.
	   @return the string representation.
	 */
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			PdfWriter w = new PdfWriter(baos);
			writePdf(w, false);
			w.close();
			baos.close();
		}
		catch (IOException e) {
			return null;
		}
		return baos.toString();
	}

	/**
	   Writes this object in PDF format.
	   @param w the <code>PdfWriter</code> to write to.
	   @param spacing specifies whether to add white-space before
	   the object.  A value of <code>true</code> enables the
	   addition of white-space.  If the object begins with a PDF
	   delimiter, then this option is ignored and no white-space
	   is written.
	   @return the number of bytes written by this method.
	   @throws IOException
	 */
	protected abstract int writePdf(PdfWriter w, boolean spacing) throws IOException;

}
