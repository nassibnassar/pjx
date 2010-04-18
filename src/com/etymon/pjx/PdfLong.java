/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents a long integer value to be used with byte offsets such
   as the <code>Prev</code> entry of a document trailer dictionary.
   @author Nassib Nassar
*/
public class PdfLong
	extends PdfNumber {

	/**
	   The long value of this object.
	*/
	protected long _n;

	/**
	   Constructs an long integer object representing a long
	   value.
	   @param n the long value.
	 */
	public PdfLong(long n) {
		_n = n;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfLong) ) ) {
			return false;
		}

		return (_n == ((PdfLong)obj)._n);
	}

	public int getInt() {
		return (int)_n;
	}

	public long getLong() {
		return _n;
	}

	public float getFloat() {
		return (float)_n;
	}

	public int hashCode() {
		return (int)( _n ^ ( _n >>> 32 ) );
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
		
		String s = Long.toString(_n);
		dos.writeBytes(s);
		return count + s.length();

	}

}
