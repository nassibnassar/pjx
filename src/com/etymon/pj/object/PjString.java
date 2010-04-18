package com.etymon.pj.object;

import java.io.*;
import com.etymon.pj.exception.*;

/**
   A representation of the PDF string type.
   @author Nassib Nassar
*/
public class PjString
	extends PjObject {

	/**
	   Creates a string object.
	   @param s the string value to initialize this object to.
	*/
	public PjString(String s) {
		_s = s;
	}

	/**
	   Returns the string value of this object.
	   @return the string value of this object.
	*/
	public String getString() {
		return _s;
	}

	/**
	   Writes this string to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	 */
	public long writePdf(OutputStream os) throws IOException {
		long z = 0;
		int length = _s.length();
		char c;
		z = z + write(os, "(");
		for (int x = 0; x < length; x++) {
			c = _s.charAt(x);
			switch (c) {
			case '\n':
				z = z + write(os, "\\n");
				break;
			case '\r':
				z = z + write(os, "\\r");
				break;
			case '\t':
				z = z + write(os, "\\t");
				break;
			case '\b':
				z = z + write(os, "\\b");
				break;
			case '\f':
				z = z + write(os, "\\f");
				break;
			case '\\':
				z = z + write(os, "\\\\");
				break;
			case '(':
				z = z + write(os, "\\(");
				break;
			case ')':
				z = z + write(os, "\\)");
				break;
			default:
				z = z + write(os, c);
			}
		}
		z = z + write(os, ")");
		return z;
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	*/
	public Object clone() {
		return this;
	}

	/**
	   Returns a string representation of this array in PDF format.
	   @return the string representation.
	public String toString() {
		int length = _s.length();
		char c;
		StringBuffer sb = new StringBuffer("(");
		for (int x = 0; x < length; x++) {
			c = _s.charAt(x);
			switch (c) {
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '(':
				sb.append("\\(");
				break;
			case ')':
				sb.append("\\)");
				break;
			default:
				if (Character.isISOControl(c)) {
					sb.append('\\');
					sb.append(Integer.toOctalString((int)c));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append(')');
		return sb.toString();
	}
	*/

	/**
	   Converts a PDF-encoded string to a java String, which may
	   be then be used to initialize a PjString object.
	   @param pdfString the PDF-encoded string to be decoded.
	   @return the sequence of characters decoded from pdfString,
	   represented as a java String.
	   @exception PdfFormatException if pdfString is invalid PDF.
	*/
	public static String decodePdf(String pdfString) throws PdfFormatException {
		int length = pdfString.length();
		if (length == 0) {
			throw new PdfFormatException("'(' or '<' expected.", 0);
		}
		switch (pdfString.charAt(0)) {
		case '(':
			if (pdfString.charAt(length - 1) != ')') {
				throw new PdfFormatException("')' expected.", length);
			}
			return decodeEscapedString(pdfString);
		case '<':
			if (pdfString.charAt(length - 1) != '>') {
				throw new PdfFormatException("'>' expected.", length);
			}
			return decodeHexString(pdfString);
		default:
			throw new PdfFormatException("'(' or '<' expected.", 0);
		}
	}

	/**
	   Decodes a PDF string enclosed in parentheses.  This method
	   ignores the first and last characters of pdfString because
	   they are assumed to be matching parentheses.
	   @param pdfString the PDF-encoded string to be decoded.
	   @return the sequence of characters decoded from pdfString,
	   represented as a java String.
	   @exception PdfFormatException if invalid PDF encoding is
	   encountered in pdfString.
	*/
	private static String decodeEscapedString(String pdfString) throws PdfFormatException {
		int length = pdfString.length();
		StringBuffer decodedString = new StringBuffer(length);
		StringBuffer escapeString = new StringBuffer(4);
		boolean escape = false;
		char ch;
		int x = 1;
		while (x < (length - 1)) {
			ch = pdfString.charAt(x);
			if (ch == '\\') {
				if (escape == false) {
					// this is the beginning of an escape string
					escape = true;
					escapeString.setLength(0);
				} else {
					// we're already escaped, so this must be the 2nd backslash in a row
					decodedString.append('\\');
					escape = false;
				}
			} else {
				if (escape == false) {
					// it's a normal character
					decodedString.append(ch);
				} else {
					// this is part of an escaped sequence
					if (escapeString.length() == 0) {
						// it's the beginning of the sequence!
						switch (ch) {
						case 'n':
							decodedString.append('\n');
							escape = false;
							break;
						case 'r':
							decodedString.append('\r');
							escape = false;
							break;
						case 't':
							decodedString.append('\t');
							escape = false;
							break;
						case 'b':
							decodedString.append('\b');
							escape = false;
							break;
						case 'f':
							decodedString.append('\f');
							escape = false;
							break;
						case '\\':
							decodedString.append('\\');
							escape = false;
							break;
						case '(':
							decodedString.append('(');
							escape = false;
							break;
						case ')':
							decodedString.append(')');
							escape = false;
							break;
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							escapeString.append(ch);
							break;
						default:
							// here we should throw a new
							// PdfFormatException("Invalid escape character.", x);
							// unfortunately, I ran this on a PDF file created using
							// Acrobat PDFWriter 2.0 for Windows, and that file had
							// solitary '\' characters in strings (in a file path in
							// a /Creator field), which is incorrect,
							// unless I am missing something.
							// so we may need to be more forgiving;
							// for now, if we reach this point, let's just treat the
							// token as a backslash and exit escape mode.
							decodedString.append('\\');
							escape = false;
							// roll back counter to reprocess this character
							x--;
						}
					} else {
						// it's just another character in the sequence;
						// so either it's an octal digit, or else we're
						// back to non-escape mode
						switch (ch) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							// octal digit
							/*
							if (escapeString.length() < 3) {
								escapeString.append(ch);
								break;
							}
							*/
							int len = escapeString.length();
							if (len < 3) {
								escapeString.append(ch);
							}
							if (len < 2) {
								break;
							}
							// otherwise we continue into the default section...
							// but first push the counter forward, so it doesn't
							// roll back to the current character.
							x++;
						default:
							// end of escape; we need to decode the octal token and move on
							decodedString.append((char)
								Integer.parseInt(escapeString.toString(), 8));
							escape = false;
							// roll back counter to reprocess this character
							x--;
						}
					}
				}
			}
			x++;
		}
		return decodedString.toString();
	}
	
	/**
	   Decodes a PDF hexadecimal string enclosed in angle
	   brackets.  This method ignores the first and last
	   characters of pdfString because they are assumed to be
	   matching angle brackets.
	   @param pdfString the PDF-encoded string to be decoded.
	   @return the sequence of characters decoded from pdfString,
	   represented as a java String.
	   @exception PdfFormatException if invalid PDF encoding is
	   encountered in pdfString.
	*/
	private static String decodeHexString(String pdfString) throws PdfFormatException {
		int length = pdfString.length();
		StringBuffer decodedString = new StringBuffer(length);
		StringBuffer hexString = new StringBuffer(4);
		char ch;
		int x = 1;
		while (x < (length - 1)) {
			ch = pdfString.charAt(x);
			// first make sure it is a hex digit
			switch (Character.toUpperCase(ch)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				// good, it is a valid hex character
				// we accumulate pairs in hexString
				hexString.append(ch);
				// if this is the last character, then pad out hexString with a zero if needed
				if ( (x == (length - 2)) && (hexString.length() == 1) ) {
					hexString.append('0');
				}
				// now, if we have a pair of digits, evaluate it and clear hexString
				if (hexString.length() == 2) {
					decodedString.append((char)Integer.parseInt(hexString.toString(), 16));
					hexString.setLength(0);
				}
				break;
			case ' ':
			case '\t':
			case '\r':
			case '\n':
			case '\f':
				// ignore whitespace
				break;
			default:
				throw new PdfFormatException("Hexadecimal digit expected.", x);
			}
			x++;
		}
		return decodedString.toString();
	}
	
	/**
	   Compares two PjString objects for equality.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.  */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PjString) {
			return _s.equals(((PjString)obj)._s);
		} else {
			return false;
		}
	}
	
	private String _s;
	
}
