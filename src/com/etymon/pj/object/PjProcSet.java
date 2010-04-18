package com.etymon.pj.object;

import java.util.*;

/**
   A representation of the PDF ProcSet type.
   @author Nassib Nassar
*/
public class PjProcSet
	extends PjArray {

	/**
	   Creates a ProcSet object.
	*/
	public PjProcSet() {
		super();
	}

	/**
	   Creates a ProcSet as a wrapper around a Vector.
	   @param v the Vector to use for this ProcSet.
	*/
	public PjProcSet(Vector v) {
		super(v);
	}

	/**
           Examines an array to see if it is a PDF ProcSet object.
           @param array the array to examine.
           @return true if the array could be interpreted as a
           valid PjProcSet object.
        */
        public static boolean isLike(PjArray array) {
		// see if all the names are legal ProcSet names
		Enumeration m = array._v.elements();
		PjName name;
		Object obj;
		while (m.hasMoreElements()) {
			obj = m.nextElement();
			if ( ! (obj instanceof PjName) ) {
				return false;
			}
			name = (PjName)obj;
			if ( ( ! name.equals(PjName.PDF) ) &&
			     ( ! name.equals(PjName.TEXT) ) &&
			     ( ! name.equals(PjName.IMAGEB) ) &&
			     ( ! name.equals(PjName.IMAGEC) ) &&
			     ( ! name.equals(PjName.IMAGEI) ) ) {
				return false;
			}
		}
		return true;
        }

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjProcSet(cloneVector());
	}

}
