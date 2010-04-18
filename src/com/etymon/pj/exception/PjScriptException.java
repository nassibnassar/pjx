package com.etymon.pj.exception;

/**
   An exception that gets thrown by PjScript.
   @author Nassib Nassar
*/
public class PjScriptException
	extends PjException {

	/**
	   Creates a PjScriptException with detailed arguments.
	   @param message a detailed message.
	   @param lineNumber the line number in the script where the exception occurred.
	   @param source the file or program where the script originated.
	   @param errorType the general class of error.
	*/
	public PjScriptException(String message, int lineNumber, String source, int errorType) {
		super(message);
		_message = message;
		_lineNumber = lineNumber;
		_source = source;
		_errorType = errorType;
	}
	
	public String getMessage() {
		return _message;
	}

	public int getLineNumber() {
		return _lineNumber;
	}

	public String getSource() {
		return _source;
	}

	public int getErrorType() {
		return _errorType;
	}

	public String getFullMessage() {
		if (_lineNumber == -1) {
			return "pjscript: " + _source + ": " + _message;
		} else {
			return "pjscript: " + _source + ":" + _lineNumber + ": " + _message;
		}
	}
	
	String _message;
	int _lineNumber;
	String _source;
	int _errorType;

}
