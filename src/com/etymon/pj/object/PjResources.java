package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF resources dictionary.
   @author Nassib Nassar
*/
public class PjResources
	extends PjDictionary {

	/**
	   Creates a new resources dictionary.
	*/
	public PjResources() {
		super();
	}

	/**
	   Creates a resources dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjResources(Hashtable h) {
		super(h);
	}

	public void setProcSet(PjProcSet procSet) {
		_h.put(PjName.PROCSET, procSet);
	}

	public void setProcSet(PjReference procSet) {
		_h.put(PjName.PROCSET, procSet);
	}

	public PjObject getProcSet() throws InvalidPdfObjectException {
		return hget(PjName.PROCSET);
	}

	public void setFont(PjDictionary font) {
		_h.put(PjName.FONT, font);
	}

	public void setFont(PjReference font) {
		_h.put(PjName.FONT, font);
	}

	public PjObject getFont() throws InvalidPdfObjectException {
		return hget(PjName.FONT);
	}

	public void setColorSpace(PjName colorSpace) {
		_h.put(PjName.COLORSPACE, colorSpace);
	}

	public void setColorSpace(PjArray colorSpace) {
		_h.put(PjName.COLORSPACE, colorSpace);
	}

	public void setColorSpace(PjReference colorSpace) {
		_h.put(PjName.COLORSPACE, colorSpace);
	}

	public PjObject getColorSpace() throws InvalidPdfObjectException {
		return hget(PjName.COLORSPACE);
	}

	public void setXObject(PjDictionary xObject) {
		_h.put(PjName.XOBJECT, xObject);
	}

	public void setXObject(PjReference xObject) {
		_h.put(PjName.XOBJECT, xObject);
	}

	public PjObject getXObject() throws InvalidPdfObjectException {
		return hget(PjName.XOBJECT);
	}

	public void setExtGState(PjDictionary extGState) {
		_h.put(PjName.EXTGSTATE, extGState);
	}

	public void setExtGState(PjReference extGState) {
		_h.put(PjName.EXTGSTATE, extGState);
	}

	public PjObject getExtGState() throws InvalidPdfObjectException {
		return hget(PjName.EXTGSTATE);
	}

	public void setPattern(PjStream pattern) {
		_h.put(PjName.PATTERN, pattern);
	}

	public void setPattern(PjReference pattern) {
		_h.put(PjName.PATTERN, pattern);
	}

	public PjObject getPattern() throws InvalidPdfObjectException {
		return hget(PjName.PATTERN);
	}

	public void setProperties(PjDictionary properties) {
		_h.put(PjName.PROPERTIES, properties);
	}

	public void setProperties(PjReference properties) {
		_h.put(PjName.PROPERTIES, properties);
	}

	public PjObject getProperties() throws InvalidPdfObjectException {
		return hget(PjName.PROPERTIES);
	}

	/**
	   Examines a dictionary to see if it is a PDF resources
	   dictionary.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjResources object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		return (dictionary.getHashtable().get(PjName.PROCSET) != null);
	}
	
	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjResources(cloneHt());
	}
	
}
