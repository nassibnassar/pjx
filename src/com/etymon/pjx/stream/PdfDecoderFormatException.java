/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import com.etymon.pjx.*;

/**
   Thrown if a stream decoder encounters data in a stream that it
   cannot decode.
   @author Nassib Nassar
*/
public class PdfDecoderFormatException
	extends PdfDecoderException {
	
	/**
	   Creates an instance of this exception with a detailed
	   message.
	   @param s the detailed message.
	*/
	public PdfDecoderFormatException(String s) {
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
	public PdfDecoderFormatException(String s, long errorOffset) {
		super(s, errorOffset);
	}

}
