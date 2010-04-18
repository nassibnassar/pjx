package com.etymon.pj.object;

import java.io.*;

/**
   A representation of the PDF reference type.
   @author Nassib Nassar
*/
public class PjReference
	extends PjObject {

	/**
	   Creates a PDF reference object.
	   @param obj the object number for the new reference.
	*/
	public PjReference(PjNumber obj) {
		_obj = obj;
		_gen = PjNumber.ZERO;
	}

	/**
	   Creates a PDF reference object.
	   @param obj the object number for the new reference.
	   @param gen the generation number for the new reference.
	*/
	public PjReference(PjNumber obj, PjNumber gen) {
		_obj = obj;
		_gen = gen;
	}

	/**
	   Returns the object number referenced by this PDF reference.
	   @return the object number within this PDF reference.
	*/
	public PjNumber getObjNumber() {
		return _obj;
	}

	/**
	   Returns the generation number referenced by this PDF reference.
	   @return the generation number within this PDF reference.
	*/
	public PjNumber getGenNumber() {
		return _gen;
	}

	/**
	   Writes this PDF reference object to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public long writePdf(OutputStream os) throws IOException {
		long z = _obj.writePdf(os);
		z = z + write(os, " ");
		z = z + _gen.writePdf(os);
		z = z + write(os, " R");
		return z;
	}

	/**
	   Returns a string representation of this reference in PDF format.
	   @return the string representation.
	public String toString() {
		return _obj.toString() + ' ' + _gen.toString() + " R";
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
	   Compares two PjReference objects for equality.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.
	*/
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PjReference) {
			return ( (_obj.equals(((PjReference)obj)._obj)) &&
				 (_gen.equals(((PjReference)obj)._gen)) );
		} else {
			return false;
		}
	}

	/**
	   Returns a hash code value for the object.
	   @return a hashcode value for this object.
	*/
	public int hashCode() {
		return _obj.hashCode();
	}
	
	private PjNumber _obj;
	private PjNumber _gen;
	
}
