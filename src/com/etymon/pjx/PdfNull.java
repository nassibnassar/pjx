/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF null object.
   @author Nassib Nassar
*/
public class PdfNull
	extends PdfObject {

	/**
	   A <code>PdfNull</code> object representing the null value.
	 */
        public static final PdfNull NULL = new PdfNull();
	
	/**
	   Constructs a null object.  <b>In most cases there is no
	   need to create a new instance of this class, and the {@link
	   #valueOf() valueOf()} method is preferred.</b>
	 */
	public PdfNull() {
	}

	/**
	   Checks whether an object represents a null value.  Note
	   that this method differs slightly from {@link
	   #equals(Object) equals(Object)} in that this returns
	   <code>true</code> when called with a <code>null</code>
	   value.  This method is useful for examining PDF objects, in
	   which <code>null</code> and <code>PdfNull</code> should
	   normally be considered equivalent.  For example, if a
	   dictionary value or an indirect object is
	   <code>PdfNull</code>, it is equivalent to the object not
	   existing.
	   @param obj the object to examine.
	   @return <code>true</code> if the object is either
	   <code>null</code> or an instance of <code>PdfNull</code>;
	   otherwise <code>false</code> is returned.
	 */
	public static boolean isNull(Object obj) {

		return ( (obj == null) || (obj instanceof PdfNull) );
		
	}
	
	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfNull) ) ) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return 4321;
	}

	/**
	   Returns a <code>PdfNull</code> object.  This method is
	   normally preferable to {@link #PdfNull() PdfNull()} because
	   it avoids allocating a new instance.
	   @return the null object.
	 */
	public static PdfNull valueOf() {
		return PdfNull.NULL;
	}

	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {

		DataOutputStream dos = w.getDataOutputStream();
		
		int count;

		if (spacing) {
			dos.write(' ');
			count = 1;
		} else {
			count = 0;
		}

		dos.writeBytes("null");
		return count + 4;
	}

}
