package com.etymon.pj.object;

import java.io.*;

/**
   A representation of the PDF number type.
   @author Nassib Nassar
*/
public class PjNumber
	extends PjObject {

	/**
	   Creates a number object.
	   @param f the numeric value to initialize this object to.
	*/
	public PjNumber(float f) {
		_f = f;
	}

	/**
	   Returns the floating point value of this object.
	   @return the floating point value of this object.
	*/
	public float getFloat() {
		return _f;
	}

	/**
	   Returns the integer value of this object.
	   @return the integer value of this object.
	*/
	public int getInt() {
		return new Float(_f).intValue();
	}

	/**
	   Returns the long value of this object.
	   @return the long value of this object.
	*/
	public long getLong() {
		return new Float(_f).longValue();
	}

	/**
	   Determines whether this number is an integer.
	   @return true if this number has no fractions.
	*/
	public boolean isInteger() {
		return ( ((float)(new Float(_f).intValue())) == _f );
	}

        /**
           Writes this number object to a stream in PDF format.
           @param os the stream to write to.
           @return the number of bytes written.
           @exception IOException if an I/O error occurs.
         */
        public long writePdf(OutputStream os) throws IOException {
                Float ft = new Float(_f);
                int x = ft.intValue();
                if ((float)x == _f) {
                        return write(os, new Integer(x));
                } else {
                        return write(os, ft);
                }
        }

	/**
	   Returns a string representation of this number in PDF format.
	   @return the string representation.
	public String toString() {
		Float ft = new Float(_f);
		int x = ft.intValue();
		if ((float)x == _f) {
			return new Integer(x).toString();
		} else {
			return ft.toString();
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
	   Compares two PjNumber objects for equality.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.
	*/
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PjNumber) {
			return (_f == ((PjNumber)obj)._f);
		} else {
			return false;
		}
	}

	/**
	   Returns a hash code value for the object.
	   @return a hashcode value for this object.
	*/
	public int hashCode() {
		return new Float(_f).hashCode();
	}
	
	public static final PjNumber ZERO = new PjNumber(0);
	public static final PjNumber ONE = new PjNumber(1);
	public static final PjNumber TWO = new PjNumber(2);
	
	private float _f;
	
}
