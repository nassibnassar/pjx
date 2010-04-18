/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

/**
   The superclass of all exceptions within this package.
   @author Nassib Nassar
*/
public class PdfException
	extends Exception {
	
	/**
	   The position where the error was found, or -1 if no
	   position information is available.
	*/
	private long _offset;
	
	/**
	   Creates a PdfException with a detailed message.
	   @param s the detailed message.
	*/
	public PdfException(String s) {
		super(s);
		_offset = -1;
	}
	
	/**
	   Creates a PdfException with a detailed message and offset.
	   A detailed message is a String that describes this
	   particular exception.
	   @param s the detailed message.
	   @param offset the position where the error is found.
	*/
	public PdfException(String s, long offset) {
		super(s);
		_offset = offset;
	}

	
	/**
	   Returns the position where the error was found.
	   @return the position where the error was found or -1 if no
	   position information is available.
	*/
	public long getOffset() {
		return _offset;
	}

}
