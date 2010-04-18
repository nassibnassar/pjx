package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF font dictionary (abstract base class).
   @author Nassib Nassar
*/
public abstract class PjFont
	extends PjDictionary {

	/**
	  Creates a new font dictionary.
	*/
	public PjFont() {
		super();
		_h.put(PjName.TYPE, PjName.FONT);
	}

	/**
	   Creates a font dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjFont(Hashtable h) {
		super(h);
	}

	public void setName(PjName name) {
		_h.put(PjName.NAME, name);
	}

	public PjObject getName() throws InvalidPdfObjectException {
		return hget(PjName.NAME);
	}

	public void setBaseFont(PjName baseFont) {
		_h.put(PjName.BASEFONT, baseFont);
	}

	public void setBaseFont(PjReference baseFont) {
		_h.put(PjName.BASEFONT, baseFont);
	}

	public PjObject getBaseFont() throws InvalidPdfObjectException {
		return hget(PjName.BASEFONT);
	}

	public void setFirstChar(PjNumber firstChar) {
		_h.put(PjName.FIRSTCHAR, firstChar);
	}

	public void setFirstChar(PjReference firstChar) {
		_h.put(PjName.FIRSTCHAR, firstChar);
	}

	public PjObject getFirstChar() throws InvalidPdfObjectException {
		return hget(PjName.FIRSTCHAR);
	}

	public void setLastChar(PjNumber lastChar) {
		_h.put(PjName.LASTCHAR, lastChar);
	}

	public void setLastChar(PjReference lastChar) {
		_h.put(PjName.LASTCHAR, lastChar);
	}

	public PjObject getLastChar() throws InvalidPdfObjectException {
		return hget(PjName.LASTCHAR);
	}

	public void setWidths(PjReference widths) {
		_h.put(PjName.WIDTHS, widths);
	}

	public PjObject getWidths() throws InvalidPdfObjectException {
		return hget(PjName.WIDTHS);
	}

	public void setEncoding(PjDictionary encoding) {
		_h.put(PjName.ENCODING, encoding);
	}

	public void setEncoding(PjName encoding) {
		_h.put(PjName.ENCODING, encoding);
	}

	public void setEncoding(PjReference encoding) {
		_h.put(PjName.ENCODING, encoding);
	}

	public PjObject getEncoding() throws InvalidPdfObjectException {
		return hget(PjName.ENCODING);
	}

	public void setFontDescriptor(PjReference fontDescriptor) {
		_h.put(PjName.FONTDESCRIPTOR, fontDescriptor);
	}

	public PjObject getFontDescriptor() throws InvalidPdfObjectException {
		return hget(PjName.FONTDESCRIPTOR);
	}

}
