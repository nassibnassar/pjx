package com.etymon.pj.object.pagemark;

import java.io.*;
import java.util.*;
import com.etymon.pj.object.*;

/**
   Image operator: EI.
   @author Nassib Nassar
*/
public class XEI
	extends PageMarkOperator {

	public XEI(PjDictionary properties, byte[] imageData) {
		_properties = properties;
		_imageData = imageData;
	}

	public PjDictionary getProperties() {
		return _properties;
	}

	public byte[] getImageData() {
		return _imageData;
	}

	public long writePdf(OutputStream os) throws IOException {
		long z = writeln(os, "BI");
		z = z + _properties.writePdf(os);
		z = z + write(os, "ID ");
		z = z + write(os, _imageData);
		z = z + writeln(os, "EI");
		return z;
	}
	
	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	*/
	public Object clone() {
		// may need to be changed since we've added image data
		return this;
	}
	
	public boolean equals(Object obj) {
		// may need to be changed since we've added image data
		if (obj == null) {
			return false;
		}
		if (obj instanceof XEI) {
			return (_properties.equals(((XEI)obj)._properties));
		} else {
			return false;
		}
	}

	private PjDictionary _properties;
	private byte[] _imageData;
	
}
