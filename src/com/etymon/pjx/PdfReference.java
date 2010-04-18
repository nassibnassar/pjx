/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF indirect reference.
   @author Nassib Nassar
*/
public class PdfReference
	extends PdfObject {

	/**
	   The generation number of this indirect reference.
	*/
	protected int _g;

	/**
	   The object number of this indirect reference.
	 */
	protected int _n;

	/**
	   Constructs an indirect reference representing a specified
	   object number and generation number.
	   @param n the object number.
	   @param g the generation number.
	 */
	public PdfReference(int n, int g) {
		_n = n;
		_g = g;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfReference) ) ) {
			return false;
		}

		PdfReference r = (PdfReference)obj;
		return ( (_n == r._n) && (_g == r._g) );
	}

	/**
	   Returns the generation number of this indirect reference.
	   @return the generation number.
	 */
	public int getGenerationNumber() {
		return _g;
	}

	/**
	   Returns the object number of this indirect reference.
	   @return the object number.
	 */
	public int getObjectNumber() {
		return _n;
	}

	public int hashCode() {
		return _n + (_g << 16);
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
		count += s.length();

		dos.write(' ');
		count++;

		s = Integer.toString(_g);
		dos.writeBytes(s);
		count += s.length();

		dos.writeBytes(" R");
		return count + 2;

	}

}
