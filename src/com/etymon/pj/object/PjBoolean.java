package com.etymon.pj.object;

import java.io.*;

/**
   A representation of the PDF Boolean type.
   @author Nassib Nassar
*/
public class PjBoolean
	extends PjObject {

	/**
	   Creates a Boolean object.
	   @param b the Boolean value to initialize this object to.
	*/
	public PjBoolean(boolean b) {
		_b = b;
	}

	/**
	   Returns the Boolean value of this object.
	   @return the Boolean value of this object.
	*/
	public boolean getBoolean() {
		return _b;
	}

	/**
	   Writes this Boolean to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	 */
	public long writePdf(OutputStream os) throws IOException {
		if (_b) {
			return write(os, "true");
		} else {
			return write(os, "false");
		}
	}

	/**
	   Returns a string representation of this Boolean in PDF format.
	   @return the string representation.
	   public String toString() {
		if (_b) {
			return "true";
		} else {
			return "false";
		}
	}
	*/

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	*/
	public Object clone() {
		return this;
	}
	
	/**
	   Compares two PjBoolean objects for equality.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.
	*/
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PjBoolean) {
			return (_b == ((PjBoolean)obj)._b);
		} else {
			return false;
		}
	}
	
        public static final PjBoolean TRUE = new PjBoolean(true);
        public static final PjBoolean FALSE = new PjBoolean(false);

	private boolean _b;
	
}
