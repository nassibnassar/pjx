package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF font descriptor (abstract base class).
   @author Carl Scholz (Texterity Inc.)
   @author Nassib Nassar (Etymon Systems, Inc.)
*/
public class PjFontDescriptor
	extends PjDictionary {

	/**
	   @deprecated
	*/
	private static String[] PjFDFlagBitNames = {
		"FixedWidth",               // (2)
		"Serif",                    // (2^2)
		"Symbolic",                 // (2^3)
		"Script",                   // (2^4)
		"Reserved5",                // (2^5)
		"RomanCharSet",             // (2^6)
		"Italic",                   // (2^7)
		"Reserved8",                // (2^8)
		"Reserved9",                // (2^9)
		"Reserved10",               // (2^10)
		"Reserved11",               // (2^11)
		"Reserved12",               // (2^12)
		"Reserved13",               // (2^13)
		"Reserved14",               // (2^14)
		"Reserved15",               // (2^15)
		"Reserved16",               // (2^16)
		"AllCap",                   // (2^17)
		"SmallCap",                 // (2^18)
		"ForceBoldAtSmallSizes",    // (2^19)
		"Reserved20",               // (2^20)
		"Reserved21",               // (2^21)
		"Reserved22",               // (2^22)
		"Reserved23",               // (2^23)
		"Reserved24",               // (2^24)
		"Reserved25",               // (2^25)
		"Reserved26",               // (2^26)
		"Reserved27",               // (2^27)
		"Reserved28",               // (2^28)
		"Reserved29",               // (2^29)
		"Reserved30",               // (2^30)
		"Reserved31",               // (2^31)
		"Reserved32"                // (2^32)
	};

	/**
	   Creates a new font descriptor dictionary.
	*/
	public PjFontDescriptor() {
		super();
		_h.put(PjName.TYPE, PjName.FONTDESCRIPTOR);
	}
	
	/**
	   Creates a font descriptor dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjFontDescriptor(Hashtable h) {
		super(h);
		_h.put(PjName.TYPE, PjName.FONTDESCRIPTOR);
	}

	/* Required --------------------------------------- */

	public void setAscent(PjNumber ascent) {
		_h.put(PjName.ASCENT, ascent);
	}

	public PjObject getAscent() throws InvalidPdfObjectException {
		return hget(PjName.ASCENT);
	}

	public void setCapHeight(PjNumber capheight) {
		_h.put(PjName.CAPHEIGHT, capheight);
	}

	public PjObject getCapHeight() throws InvalidPdfObjectException {
		return hget(PjName.CAPHEIGHT);
	}

	public void setDescent(PjNumber descent) {
		_h.put(PjName.DESCENT, descent);
	}

	public PjObject getDescent() throws InvalidPdfObjectException {
		return hget(PjName.DESCENT);
	}

	public void setFlags(PjNumber flags) {
		_h.put(PjName.FLAGS, flags);
	}

	public PjObject getFlags() throws InvalidPdfObjectException {
		return hget(PjName.FLAGS);
	}

	public static boolean flagsFixedWidth(int flags) {
		return ((flags & (1 << 0)) != 0);
	}
	
	public static boolean flagsSerif(int flags) {
		return ((flags & (1 << 1)) != 0);
	}
	
	public static boolean flagsSymbolic(int flags) {
		return ((flags & (1 << 2)) != 0);
	}
	
	public static boolean flagsScript(int flags) {
		return ((flags & (1 << 3)) != 0);
	}
	
	public static boolean flagsRomanCharSet(int flags) {
		return ((flags & (1 << 5)) != 0);
	}
	
	public static boolean flagsItalic(int flags) {
		return ((flags & (1 << 6)) != 0);
	}
	
	public static boolean flagsAllCap(int flags) {
		return ((flags & (1 << 16)) != 0);
	}
	
	public static boolean flagsSmallCap(int flags) {
		return ((flags & (1 << 17)) != 0);
	}
	
	public static boolean flagsForceBoldAtSmallSizes(int flags) {
		return ((flags & (2 << 18)) != 0);
	}

	/**
	   @deprecated
	*/
	public String getFlagBits() throws InvalidPdfObjectException {
		int _flags = ((PjNumber)hget(PjName.FLAGS)).getInt();
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i <= 31; i++) {
			// bit range is 1 to 32 in spec, so that's a loop from 0 to
			// 31 here.
			
			if ((_flags & 1) == 1) {
				buf.append(PjFDFlagBitNames[i] + " ");
			}
			_flags = _flags >> 1;
		}
		if (buf.length() > 0) {
			return buf.toString();
		} else {
			return null;
		}
	}
	
	public void setFontBBox(PjRectangle fontbbox) {
		_h.put(PjName.FONTBBOX, fontbbox);
	}

	public PjObject getFontBBox() throws InvalidPdfObjectException {
		return hget(PjName.FONTBBOX);
	}

	public void setFontName(PjName fontname) {
		_h.put(PjName.FONTNAME, fontname);
	}

	public PjObject getFontName() throws InvalidPdfObjectException {
		return hget(PjName.FONTNAME);
	}

	public void setItalicAngle(PjNumber italicangle) {
		_h.put(PjName.ITALICANGLE, italicangle);
	}

	public PjObject getItalicAngle() throws InvalidPdfObjectException {
		return hget(PjName.ITALICANGLE);
	}

	public void setStemV(PjNumber stemv) {
		_h.put(PjName.STEMV, stemv);
	}

	public PjObject getStemV() throws InvalidPdfObjectException {
		return hget(PjName.STEMV);
	}

	/* Optional --------------------------------------- */

	public void setAvgWidth(PjNumber avgWidth) {
		_h.put(PjName.AVGWIDTH, avgWidth);
	}

	public PjObject getAvgWidth() throws InvalidPdfObjectException {
		return hget(PjName.AVGWIDTH);
	}

	public void setCharSet(PjString charSet) {
		_h.put(PjName.CHARSET, charSet);
	}

	public PjObject getCharSet() throws InvalidPdfObjectException {
		return hget(PjName.CHARSET);
	}

	public void setFontFile(PjStream fontFile) {
		_h.put(PjName.FONTFILE, fontFile);
	}

	public PjObject getFontFile() throws InvalidPdfObjectException {
		return hget(PjName.FONTFILE);
	}

	public void setFontFile2(PjStream fontFile2) {
		_h.put(PjName.FONTFILE2, fontFile2);
	}

	public PjObject getFontFile2() throws InvalidPdfObjectException {
		return hget(PjName.FONTFILE2);
	}

	public void setFontFile3(PjStream fontFile3) {
		_h.put(PjName.FONTFILE3, fontFile3);
	}

	public PjObject getFontFile3() throws InvalidPdfObjectException {
		return hget(PjName.FONTFILE3);
	}

	public void setLeading(PjNumber leading) {
		_h.put(PjName.LEADING, leading);
	}

	public PjObject getLeading() throws InvalidPdfObjectException {
		return hget(PjName.LEADING);
	}

	public void setMaxWidth(PjNumber maxWidth) {
		_h.put(PjName.MAXWIDTH, maxWidth);
	}

	public PjObject getMaxWidth() throws InvalidPdfObjectException {
		return hget(PjName.MAXWIDTH);
	}

	public void setMissingWidth(PjNumber missingWidth) {
		_h.put(PjName.MISSINGWIDTH, missingWidth);
	}

	public PjObject getMissingWidth() throws InvalidPdfObjectException {
		return hget(PjName.MISSINGWIDTH);
	}

	public void setStemH(PjNumber stemH) {
		_h.put(PjName.STEMH, stemH);
	}

	public PjObject getStemH() throws InvalidPdfObjectException {
		return hget(PjName.STEMH);
	}

	public void setXHeight(PjNumber xHeight) {
		_h.put(PjName.XHEIGHT, xHeight);
	}

	public PjObject getXHeight() throws InvalidPdfObjectException {
		return hget(PjName.XHEIGHT);
	}

	/**
	   Examines a dictionary to see if it is a PDF font descriptor.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjFontDescriptor object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		Hashtable h = dictionary.getHashtable();
		// check if the Type is FontDescriptor
		try {
			PjName type = (PjName)(h.get(PjName.TYPE));
			if (type == null) {
				return false;
			}
			if ( ! type.equals(PjName.FONTDESCRIPTOR) ) {
				return false;
			}
		}
		catch (ClassCastException e) {
			return false;
		}
		return true;
	}

}
