package com.etymon.pj;

import com.etymon.pj.object.*;

/**
   General constants used by the PJ classes.
   @author Nassib Nassar
 */
public class PjConst {

	/**
	   The PJ version number.
	*/
	public static final String VERSION = "1.10";

	/**
	   The PJ copyright notice, which is inserted into the
	   Producer (and sometimes Creator) field of the Info
	   dictionary in all PDF files output by PJ; you may not
	   remove this copyright notice.
	*/
	public static final PjString COPYRIGHT_IN_INFO =
	new PjString("Etymon PJ " + VERSION +
		     ", Copyright (C) 1998-2000 Etymon Systems, Inc. <http://www.etymon.com/>");
	
	/**
	   The PJ version number in PDF format, which is inserted into
	   all PDF files output by PJ.
	*/
	public static final String VERSION_IN_PDF = "%PJ-" + VERSION;

	/**
	   The PJ copyright notice, which is inserted into all PDF
	   files output by PJ; you may not remove this copyright
	   notice.
	*/
	public static final String COPYRIGHT_IN_PDF =
	"%Generated with Etymon PJ " + VERSION +
	", Copyright (C) 1998-2000 Etymon Systems, Inc. <http://www.etymon.com/>";

	/**
	   The PDF version output by this release of PJ.
	*/
	public static final String PDF_VERSION = "1.3";
	
	/**
	   The number of bytes from the end of a PDF file at which to
	   start scanning for startxref.
	*/
	public static final int SCAN_STARTXREF = 40;

	/**
	   The number of times to back up and retry scanning for
	   startxref.  Each time, the parser will back up to a point
	   (SCAN_STARTXREF) bytes before the previous time.
	*/
	public static final int SCAN_STARTXREF_RETRY = 10;

	/**
	   The size of the byte[] used for flate compression and
	   decompression.
	*/
	public static final int FLATE_BUFFER_SIZE = 16384;
	
	/**
	   The end-of-line sequence to use when writing a PDF file to
	   disk.
	*/
	public static final String PDF_EOL = "\n";

	/**
	   The string length of PDF_EOL.
	*/
	public static final int PDF_EOL_LEN = PDF_EOL.length();

}
