/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   Represents the PDF name object.
   @author Nassib Nassar
*/
public class PdfName
	extends PdfObject {

	/**
	   The string value of this name object.
	*/
	protected String _s;
	
	/**
	   Constructs a name object representing a string value.
	   @param s the string value.
	 */
	public PdfName(String s) {
		_s = s;
	}

	public boolean equals(Object obj) {

		if ( (obj == null) || ( !(obj instanceof PdfName) ) ) {
			return false;
		}

		return _s.equals( ((PdfName)obj)._s );
	}

	/**
	   Returns the string value of this name object.
	   @return the string value.
	 */
	public String getString() {
		return _s;
	}

	public int hashCode() {
		return _s.hashCode();
	}

	/**
	   Converts a name object in PDF format to a string value as
	   stored by this class.
	   @param pdf the name object in PDF format.
	 */
	protected static String pdfToString(String pdf) throws PdfFormatException {

		int len = pdf.length();
		if (len < 1) {
			throw new PdfFormatException("Invalid PDF name (length < 1).", 0);
		}
		if (pdf.charAt(0) != '/') {
			throw new PdfFormatException("Invalid PDF name (missing initial '/').", 0);
		}
		StringBuffer sb = new StringBuffer(len);
		int x = 1;
		do {
			
			char ch = pdf.charAt(x);

			if (ch == '#') {

				if ( (x + 2) >= len ) {
					throw new PdfFormatException("Invalid PDF name (incorrect use of '#').", x);
				}

				try {

					Integer code = Integer.valueOf(pdf.substring(x + 1, x + 3), 16);
					sb.append( (char)code.byteValue() );

					x += 3;
					
				}
				catch (NumberFormatException e) {
					throw new PdfFormatException("Invalid PDF name (incorrect use of '#').", x);
				}
				
			} else {

				sb.append(ch);
				
				x++;
			
			}

		} while (x < len);

		return sb.toString();
		
	}

	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {

		DataOutputStream dos = w.getDataOutputStream();

		int count = 1;
		dos.write('/');

		String s = _s;
		int len = s.length();
		String hex;
		boolean special;
		for (int x = 0; x < len; x++) {

			char ch = s.charAt(x);

			switch (ch) {
			case '\u0000': // null is technically not allowed
			case '\t':
			case '\n':
			case '\f':
			case '\r':
			case ' ':
			case '(':
			case ')':
			case '<':
			case '>':
			case '[':
			case ']':
			case '{':
			case '}':
			case '/':
			case '%':
			case '#':
				special = true;
				break;
			default:
				special = ( (ch < 33) || (ch > 126) );
			}

			if (special) {
				dos.write('#');
				if (ch < 16) {
					dos.write('0');
					count++;
				}
				hex = Integer.toHexString(ch);
				dos.writeBytes(hex);
				count += hex.length() + 1; // 1 for '#'
			} else {
				dos.write(ch);
				count++;
			}
			
		}

		return count;
		
	}

}
