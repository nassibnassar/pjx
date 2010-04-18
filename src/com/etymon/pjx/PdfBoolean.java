/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF Boolean object.
   @author Nassib Nassar
*/
public class PdfBoolean
	extends PdfObject {

	/**
	   The Boolean value of this object.
	*/
	protected boolean _b;

	/**
	   A <code>PdfBoolean</code> object representing the Boolean
	   value <code>false</code>.
	 */
	public static final PdfBoolean FALSE = new PdfBoolean(false);
	
	/**
	   A <code>PdfBoolean</code> object representing the Boolean
	   value <code>true</code>.
	 */
	public static final PdfBoolean TRUE = new PdfBoolean(true);

	/**
	   Constructs a Boolean object representing a Boolean value.
	   <b>In most cases there is no need to create a new instance
	   of this class, and the {@link #valueOf(boolean)
	   valueOf(boolean)} method is preferred.</b>
	   @param b the Boolean value.
	 */
	public PdfBoolean(boolean b) {
		_b = b;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfBoolean) ) ) {
			return false;
		}

		return (_b == ((PdfBoolean)obj)._b);
	}

	/**
	   Returns the Boolean value of this object.
	   @return the Boolean value.
	 */
	public boolean getBoolean() {
		return _b;
	}

	public int hashCode() {
		return _b ? 1231 : 1237;
	}

	/**
	   Returns a <code>PdfBoolean</code> object with the specified
	   value.  This method is normally preferable to {@link
	   #PdfBoolean(boolean) PdfBoolean(boolean)} because it avoids
	   allocating a new instance.
	   @param b the Boolean value.
	   @return the Boolean object.
	 */
	public static PdfBoolean valueOf(boolean b) {
		return b ? PdfBoolean.TRUE : PdfBoolean.FALSE;
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
		
		if (_b) {
			dos.writeBytes("true");
			return count + 4;
		} else {
			dos.writeBytes("false");
			return count + 5;
		}

	}

}
