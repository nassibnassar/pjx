package com.etymon.pj.object;

import java.io.*;

/**
   A representation of the PDF null type.
   @author Nassib Nassar
*/
public class PjNull
	extends PjObject {

	/**
	   Creates a null object.
	*/
	public PjNull() {
	}

        /**
           Writes this object (null) to a stream in PDF format.
           @param os the stream to write to.
           @return the number of bytes written.
           @exception IOException if an I/O error occurs.
         */
        public long writePdf(OutputStream os) throws IOException {
                return write(os, "null");
        }

	/**
	   Returns a string representation of this null object in PDF format.
	   @return the string representation.
	public String toString() {
		return "null";
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
	   Compares two PjNull objects for equality.  They are
	   automatically considered to be equal if both objects are
	   truly instances of PjNull.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.
	*/
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return (obj instanceof PjNull);
	}
	
}
