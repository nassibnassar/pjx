package com.etymon.pj.exception;

/**
   An exception that gets thrown when the parser encounters invalid
   PDF data.
   @author Nassib Nassar
*/
public class PdfFormatException
	extends PjException {

	/**
	   Creates a PdfFormatException with a detailed message.
	   @param s the detailed message.
	*/
	public PdfFormatException(String s) {
		super(s);
	}
	
	/**
	   Creates a PdfFormatException with a detailed message and
	   offset.  A detailed message is a String that describes this
	   particular exception.
	   @param s the detailed message.
	   @param errorOffset the position where the error is found
	   while parsing.
	*/
	public PdfFormatException(String s, int errorOffset) {
		super(s);
		_errorOffset = errorOffset;
	}

	/**
	   Returns the position where the error was found.
	   @return the position where the error was found or -1 if no
	   position information is available.
	*/
	public int getErrorOffset() {
		return _errorOffset;
	}

	private int _errorOffset = -1;
	
}
