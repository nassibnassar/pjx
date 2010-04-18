/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF real object.
   @author Nassib Nassar
*/
public class PdfFloat
	extends PdfNumber {

	/**
	   The float value of this object.
	*/
	protected float _n;
	
	/**
	   Constructs a real object representing a float value.
	   @param n the float value.
	 */
	public PdfFloat(float n) {
		_n = n;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfFloat) ) ) {
			return false;
		}

		return (_n == ((PdfFloat)obj)._n);
	}

	public int getInt() {
		return (int)_n;
	}

	public long getLong() {
		return (long)_n;
	}

	public float getFloat() {
		return _n;
	}

	public int hashCode() {
		return Float.floatToIntBits(_n);
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
		
		String s = Float.toString(_n);
		dos.writeBytes(s);
		return count + s.length();
	}

}
