package com.etymon.pj.object;

import java.io.*;

/**
   A representation of the PDF name type.
   @author Nassib Nassar
*/
public class PjName
	extends PjObject {

	/**
	   Creates a name object.
	   @param s the string value to initialize this object to.
	*/
	public PjName(String s) {
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
	   Writes this name to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	 */
	public long writePdf(OutputStream os) throws IOException {
		long z = write(os, "/");
		int length = _s.length();
		char c;
		for (int x = 0; x < length; x++) {
			c = _s.charAt(x);
			if (Character.isISOControl(c)) {
				z = z + write(os, "#" + Integer.toHexString((int)c));
			} else {
				z = z + write(os, c);
			}
		}
		return z;
	}

	/**
	   Returns a string representation of this name in PDF format.
	   @return the string representation.
	public String toString() {
		StringBuffer sb = new StringBuffer("/");
		int length = _s.length();
		char c;
		for (int x = 0; x < length; x++) {
			c = _s.charAt(x);
			if (Character.isISOControl(c)) {
				sb.append("#" + Integer.toHexString((int)c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	 */

	/**
	   Compares two PjName objects for equality.
	   @param obj the reference object to compare to.
	   @return true if this object is the same as obj, false
	   otherwise.
	*/
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PjName) {
			return _s.equals(((PjName)obj)._s);
		} else {
			return false;
		}
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	*/
	public Object clone() {
		return this;
	}
	
	/**
	   Returns a hash code value for the object.
	   @return a hashcode value for this object.
	*/
	public int hashCode() {
		return _s.hashCode();
	}
	
	public static final PjName AA = new PjName("AA");
	public static final PjName ACROFORM = new PjName("AcroForm");
	public static final PjName ANNOTS = new PjName("Annots");
	public static final PjName AP = new PjName("AP");
	public static final PjName ASCENT = new PjName("Ascent");
	public static final PjName ASCII85DECODE = new PjName("ASCII85Decode");
	public static final PjName ASCIIHEXDECODE = new PjName("ASCIIHexDecode");
	public static final PjName AUTHOR = new PjName("Author");
	public static final PjName AVGWIDTH = new PjName ("AvgWidth");
	public static final PjName B = new PjName("B");
	public static final PjName BASEENCODING = new PjName("BaseEncoding");
	public static final PjName BASEFONT = new PjName("BaseFont");
	public static final PjName CAPHEIGHT = new PjName("CapHeight");
	public static final PjName CATALOG = new PjName("Catalog");
	public static final PjName CCITTFAXDECODE = new PjName("CCITTFaxDecode");
	public static final PjName CHARSET = new PjName ("CharSet");
	public static final PjName COLORSPACE = new PjName("ColorSpace");
	public static final PjName CONTENTS = new PjName("Contents");
	public static final PjName COUNT = new PjName("Count");
	public static final PjName CREATIONDATE = new PjName("CreationDate");
	public static final PjName CREATOR = new PjName("Creator");
	public static final PjName CROPBOX = new PjName("CropBox");
	public static final PjName DA = new PjName("DA");
	public static final PjName DCTDECODE = new PjName("DCTDecode");
	public static final PjName DECODEPARMS = new PjName("DecodeParms");
	public static final PjName DESCENT = new PjName("Descent");
	public static final PjName DESTS = new PjName("Dests");
	public static final PjName DIFFERENCES = new PjName("Differences");
	public static final PjName DR = new PjName("DR");
	public static final PjName DUR = new PjName("Dur");
	public static final PjName DV = new PjName("DV");
	public static final PjName ENCODING = new PjName("Encoding");
	public static final PjName ENCRYPT = new PjName("Encrypt");
	public static final PjName EXTGSTATE = new PjName("ExtGState");
	public static final PjName F = new PjName("F");
	public static final PjName FDECODEPARMS = new PjName("FDecodeParms");
	public static final PjName FIELDS = new PjName("Fields");
	public static final PjName FF = new PjName("Ff");
	public static final PjName FFILTER = new PjName("FFilter");
	public static final PjName FILTER = new PjName("Filter");
	public static final PjName FIRSTCHAR = new PjName("FirstChar");
	public static final PjName FLAGS = new PjName("Flags");
	public static final PjName FLATEDECODE = new PjName("FlateDecode");
	public static final PjName FONT = new PjName("Font");
	public static final PjName FONTBBOX = new PjName("FontBBox");
	public static final PjName FONTDESCRIPTOR = new PjName("FontDescriptor");
	public static final PjName FONTFILE = new PjName ("FontFile");
	public static final PjName FONTFILE2 = new PjName ("FontFile2");
	public static final PjName FONTFILE3 = new PjName ("FontFile3");
	public static final PjName FONTNAME = new PjName("FontName");
	public static final PjName FT = new PjName("FT");
	public static final PjName H = new PjName("H");
	public static final PjName HID = new PjName("Hid");
	public static final PjName I = new PjName("I");
	public static final PjName ID = new PjName("ID");
	public static final PjName IMAGEB = new PjName("ImageB");
	public static final PjName IMAGEC = new PjName("ImageC");
	public static final PjName IMAGEI = new PjName("ImageI");
	public static final PjName INFO = new PjName("Info");
	public static final PjName ITALICANGLE = new PjName("ItalicAngle");
	public static final PjName KEYWORDS = new PjName("Keywords");
	public static final PjName KIDS = new PjName("Kids");
	public static final PjName LASTCHAR = new PjName("LastChar");
	public static final PjName LEADING = new PjName ("Leading");
	public static final PjName LENGTH = new PjName("Length");
	public static final PjName LZWDECODE = new PjName("LZWDecode");
	public static final PjName MAXLEN = new PjName ("MaxLen");
	public static final PjName MAXWIDTH = new PjName ("MaxWidth");
	public static final PjName MEDIABOX = new PjName("MediaBox");
	public static final PjName MISSINGWIDTH = new PjName ("MissingWidth");
	public static final PjName MODDATE = new PjName("ModDate");
	public static final PjName N = new PjName("N");
	public static final PjName NAME = new PjName("Name");
	public static final PjName NAMES = new PjName("Names");
	public static final PjName O = new PjName("O");
	public static final PjName OPENACTION = new PjName("OpenAction");
	public static final PjName OPT = new PjName("Opt");
	public static final PjName OUTLINES = new PjName("Outlines");
	public static final PjName P = new PjName("P");
	public static final PjName PAGE = new PjName("Page");
	public static final PjName PAGEMODE = new PjName("PageMode");
	public static final PjName PAGES = new PjName("Pages");
	public static final PjName PARENT = new PjName("Parent");
	public static final PjName PATTERN = new PjName("Pattern");
	public static final PjName PDF = new PjName("PDF");
	public static final PjName PREV = new PjName("Prev");
	public static final PjName PROCSET = new PjName("ProcSet");
	public static final PjName PRODUCER = new PjName("Producer");
	public static final PjName PROPERTIES = new PjName("Properties");
	public static final PjName Q = new PjName("Q");
	public static final PjName RECT = new PjName("Rect");
	public static final PjName RESOURCES = new PjName("Resources");
	public static final PjName ROOT = new PjName("Root");
	public static final PjName ROTATE = new PjName("Rotate");
	public static final PjName RUNLENGTHDECODE = new PjName("RunLengthDecode");
	public static final PjName STEMH = new PjName ("StemH");
	public static final PjName STEMV = new PjName("StemV");
	public static final PjName SUBJECT = new PjName("Subject");
	public static final PjName SUBTYPE = new PjName("Subtype");
	public static final PjName TEXT = new PjName("Text");
	public static final PjName THUMB = new PjName("Thumb");
	public static final PjName THREADS = new PjName("Threads");
	public static final PjName TITLE = new PjName("Title");
	public static final PjName TOPINDEX = new PjName("TopIndex");
	public static final PjName TRANS = new PjName("Trans");
	public static final PjName TYPE = new PjName("Type");
	public static final PjName TYPE1 = new PjName("Type1");
	public static final PjName URI = new PjName("URI");
	public static final PjName V = new PjName("V");
	public static final PjName VIEWERPREFERENCES = new PjName("VIEWERPREFERENCES");
	public static final PjName W = new PjName("W");
	public static final PjName WIDTHS = new PjName("Widths");
	public static final PjName XHEIGHT = new PjName ("XHeight");
	public static final PjName XOBJECT = new PjName("XObject");
	
	private String _s;
	
}
