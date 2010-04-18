package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;

/**
   The base class for all high level objects and page markings.
   @author Nassib Nassar
*/
public abstract class BaseObject
	implements Cloneable {

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public abstract Object clone() throws CloneNotSupportedException;
	
	/**
	   Writes this object to a file in PDF format.
	   @param raf the file to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	 */
	public abstract long writePdf(OutputStream os) throws
		IOException;

	/**
	   Writes a char to a stream.
	   @param os the stream to write to.
	   @param c the character to write.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public static long write(OutputStream os, char c)
		throws IOException {
		os.write((int)c);
		return 1;
	}
	
	/**
	   Writes a byte[] to a stream.
	   @param os the stream to write to.
	   @param b the byte[] to write.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public static long write(OutputStream os, byte[] b)
		throws IOException {
		os.write(b);
		return b.length;
	}
	
	/**
	   Writes an Object to a stream.
	   @param os the stream to write to.
	   @param obj the Object to write.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public static long write(OutputStream os, Object obj)
		throws IOException {
		return write(os, obj.toString().getBytes());
	}

	/**
	   Writes an Object to a stream followed by a carriage return.
	   @param os the stream to write to.
	   @param obj the Object to write.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public static long writeln(OutputStream os, Object obj)
		throws IOException {
		return write(os, obj) + write(os, PjConst.PDF_EOL);
	}

	/**
	   Returns a string representation of this object in PDF format.
	   @return the string representation.
	 */
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writePdf(baos);
		}
		catch (IOException e) {
			return null;
		}
		return baos.toString();
	}

}
