package com.etymon.pj.exception;

/**
   The base class for all exceptions within this package.
   @author Nassib Nassar
*/
public class PjException
	extends Exception {

	/**
	   Creates a PjException with a detailed message.
	   @param s the detailed message.
	*/
	public PjException(String s) {
		super(s);
	}
	
}
