/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;

/**
   Represents the PDF string object.
   @author Nassib Nassar
*/
public class PdfString
	extends PdfObject {

	/**
	   The string value of this PDF string object.
	*/
	protected String _s;

	/**
	   Constructs a PDF string object representing a string value.
	   @param s the string value.
	 */
	public PdfString(String s) {
		_s = s;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfString) ) ) {
			return false;
		}

		return _s.equals( ((PdfString)obj)._s );
	}

	/**
	   Returns the string value of this PDF string object.
	   @return the string value.
	 */
	public String getString() {
		return _s;
	}

	public int hashCode() {
		return _s.hashCode();
	}

	/**
	   Determines whether a character is a white-space character.
	   @param ch the character to examine.
	   @return <code>true</code> if the character is a white-space
	   character.
	*/
	protected static boolean isWhiteSpace(char ch) {
		switch(ch) {
		case 0:
		case '\t':
		case '\n':
		case '\f':
		case '\r':
		case ' ':
			return true;
		default:
			return false;
		}
	}
	
	/**
	   Converts a PDF string object in PDF format to a string
	   value as stored by this class.
	   @param buf contains the PDF string object in PDF format.
	 */
	protected static String pdfToString(CharBuffer buf) throws PdfFormatException {

		try {
			
			// advance past any white-space
			char ch;
			do {
				ch = buf.get();
			} while (isWhiteSpace(ch));
			// now ch should be either '(' or '<'

			if (ch == '(') {
				return decodeLiteralString(buf);
			}
			else if (ch == '<') {
				return decodeHexString(buf);
			}
			else {
				throw new PdfFormatException("'(' or '<' expected at beginning of string.");
			}
			
		}
		catch (BufferUnderflowException e) {
			throw new PdfFormatException("End of buffer reached while parsing string.");
		}

	}

	protected static String decodeLiteralString(CharBuffer buf)
		throws BufferUnderflowException, PdfFormatException {

		char ch, chc, che;
		boolean append;
		boolean escaping = false;
		int paren = 0;  // tracks number of nested parentheses
		StringBuffer sb = new StringBuffer();
		char[] code = new char[3];
		int codeLen;
		boolean done = false;
		
		do {

			ch = buf.get();

			append = true;
			
			che = ch;
				
			if (escaping) {
				
				escaping = false;
				
				switch (ch) {
				case 'n':
					che = '\n';
					break;
				case 'r':
					che = '\r';
					break;
				case 't':
					che = '\t';
					break;
				case 'b':
					che = '\b';
					break;
				case 'f':
					che = '\f';
					break;
				case '(':
					che = '(';
					break;
				case ')':
					che = ')';
					break;
				case '\\':
					che = '\\';
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
					append = false;
					code[0] = ch;
					codeLen = 1;
					boolean code_done = false;
					do {
						chc = buf.get();
						switch (chc) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
							if (codeLen < 3) {
								code[codeLen++] = chc;
								break;
							}
						default:
							code_done = true;
							buf.position( buf.position() - 1 );
						}
					} while (!code_done);
					sb.append( (char)Integer.parseInt(new String(code, 0, codeLen), 8) );
					break;
				case '\r':
				case '\n':
					append = false;
					do {
						chc = buf.get();
					} while ( (chc == '\r') || (chc == '\n') );
					buf.position ( buf.position() - 1 );
					break;
				default:
				}
				
			} else {
				
				switch (ch) {
				case '(':
					paren++;
					break;
				case ')':
					if (paren > 0) {
						paren--;
					} else {
						append = false;
						done = true;
					}
					break;
				case '\\':
					escaping = true;
					append = false;
					break;
				case '\r':
					if (buf.get() != '\n') {
						buf.position( buf.position() - 1 );
					}
					che = '\n';
					break;
				default:
				}
				
			} // if
			
			if (append) {
				sb.append(che);
			}
			
		} while (!done);

		return sb.toString();
		
	}

	protected static String decodeHexString(CharBuffer buf)
		throws BufferUnderflowException, PdfFormatException {

		char ch;
		boolean done = false;
		StringBuffer sb = new StringBuffer();
		char[] hex = new char[2];
		int hexLen = 0;
		
		do {

			ch = buf.get();

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
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				hex[hexLen] = ch;
				hexLen++;
				break;
			case '>':
				done = true;
			case 0:
			case '\t':
			case '\n':
			case '\f':
			case '\r':
			case ' ':
				break;
			default:
				throw new PdfFormatException("Unrecognized character in hexadecimal string.");
			}

			if ( (done) && (hexLen == 1) ) {
				hex[1] = '0';
				hexLen = 2;
			}

			if (hexLen >= 2) {
				sb.append( (char)Integer.parseInt(new String(hex), 16) );
				hexLen = 0;
			}
			
		} while (!done);
				
		return sb.toString();

	}
	
	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {

		DataOutputStream dos = w.getDataOutputStream();

                int count = 1;
		dos.write('(');

		String s = _s;
		int len = s.length();

		for (int x = 0; x < len; x++) {

			char ch = s.charAt(x);
			
			switch (ch) {
			case '(':
				dos.writeBytes("\\(");
				count += 2;
				break;
			case ')':
				dos.writeBytes("\\)");
				count += 2;
				break;
			case '\\':
				dos.writeBytes("\\\\");
				count += 2;
				break;
			case '\r':
				dos.writeBytes("\\r");
				count += 2;
				break;
			case '\n':
				dos.writeBytes("\\n");
				count += 2;
				break;
			default:
				dos.write(ch);
				count++;
			}
			
		} // for

		dos.write(')');
		return count + 1;
		
	}
	
}
