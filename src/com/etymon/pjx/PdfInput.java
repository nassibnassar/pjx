/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;

/**
   Specifies methods used by {@link PdfReader PdfReader} to read
   portions of a PDF document.  A class that implements this interface
   should represent one PDF document per instance of the class.
   @author Nassib Nassar
 */
public interface PdfInput {

	/**
	   Returns a name string associated of the PDF document.
	   @return the name of the PDF document.
	 */
	public String getName();
	
	/**
	   Returns the length of the PDF document.
	   @return the length (in bytes) of the PDF document.
	 */
	public long getLength();
	
	/**
	   Returns a specified portion of a PDF document as a
	   <code>ByteBuffer</code>.
	   @param start the offset position of the first byte to read.
	   @param end the offset position at which to stop reading.
 	   (The byte at this offset is not included.)
	   @return the requested portion of the PDF document.
	   @throws IOException
	*/
	public ByteBuffer readBytes(long start, long end) throws IOException;

	/**
	   Returns a specified portion of a PDF document as a
	   <code>CharBuffer</code>.
	   @param start the offset position of the first byte to read.
	   @param end the offset position at which to stop reading.
 	   (The byte at this offset is not included.)
	   @return the requested portion of the PDF document.
	   @throws IOException
	*/
	public CharBuffer readChars(long start, long end) throws IOException;

}
