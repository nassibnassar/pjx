package com.etymon.pj.exception;

/**
   An exception that gets thrown when the parser encounters invalid
   data while trying to read the xref table.
   @author Nassib Nassar
*/
public class XrefFormatException
	extends PjException {

	/**
	   Creates an XrefFormatException with a detailed message.
	   @param s the detailed message.
	*/
	public XrefFormatException(String s) {
		super(s);
	}

}
