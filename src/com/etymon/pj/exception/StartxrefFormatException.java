package com.etymon.pj.exception;

/**
   An exception that gets thrown when the parser encounters invalid
   data while trying to read the startxref.
   @author Nassib Nassar
*/
public class StartxrefFormatException
	extends PjException {

	/**
	   Creates a StartxrefFormatException with a detailed message.
	   @param s the detailed message.
	*/
	public StartxrefFormatException(String s) {
		super(s);
	}

}
