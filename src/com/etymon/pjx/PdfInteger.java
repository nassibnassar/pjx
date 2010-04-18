/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF integer object.
   @author Nassib Nassar
*/
public class PdfInteger
	extends PdfNumber {

	/**
	   The int value of this object.
	*/
	protected int _n;

        /**
           A <code>PdfInteger</code> object representing the int value
           <code>0</code>.
         */
        public static final PdfInteger ZERO = new PdfInteger(0);

	/**
	   Constructs an integer object representing an int value.
	   @param n the int value.
	 */
	public PdfInteger(int n) {
		_n = n;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfInteger) ) ) {
			return false;
		}

		return (_n == ((PdfInteger)obj)._n);
	}

	public int getInt() {
		return _n;
	}

	public long getLong() {
		return (long)_n;
	}

	public float getFloat() {
		return (float)_n;
	}

	public int hashCode() {
		return _n;
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
		
		String s = Integer.toString(_n);
		dos.writeBytes(s);
		return count + s.length();
	}

}
