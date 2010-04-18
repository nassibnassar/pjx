/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

/**
   Thrown if a PDF object is encountered that does not conform to the
   PDF specification.
   @author Nassib Nassar
*/
public class PdfFormatException
	extends PdfException {
	
	/**
	   Creates a PdfExceptionFormat with a detailed message.
	   @param s the detailed message.
	*/
	public PdfFormatException(String s) {
		super(s);
	}
	
	/**
	   Creates a PdfExceptionFormat with a detailed message and
	   offset.  A detailed message is a String that describes this
	   particular exception.
	   @param s the detailed message.
	   @param errorOffset the position where the error is found
	   while parsing.
	*/
	public PdfFormatException(String s, long errorOffset) {
		super(s, errorOffset);
	}

}
