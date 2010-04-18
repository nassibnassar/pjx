package com.etymon.pj.exception;

/**
   An exception that gets thrown when the parser encounters invalid
   data while trying to read the startxref.
   @author Nassib Nassar
*/
public class InvalidPdfObjectException
	extends PjException {

	/**
	   Creates a InvalidObjectException with a detailed message.
	   @param s the detailed message.
	*/
	public InvalidPdfObjectException(String s) {
		super(s);
	}

}
