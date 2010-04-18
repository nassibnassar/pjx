/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import com.etymon.pjx.*;

/**
   Thrown if there is a problem related to PDF stream encoding or
   decoding.
   @author Nassib Nassar
*/
public class PdfDecoderException
	extends PdfException {
	
	/**
	   Creates an instance of this exception with a detailed
	   message.
	   @param s the detailed message.
	*/
	public PdfDecoderException(String s) {
		super(s);
	}
	
	/**
	   Creates an instance of this exception with a detailed
	   message and offset.  A detailed message is a String that
	   describes this particular exception.
	   @param s the detailed message.
	   @param errorOffset the position where the error is found
	   while parsing.
	*/
	public PdfDecoderException(String s, long errorOffset) {
		super(s, errorOffset);
	}

}
