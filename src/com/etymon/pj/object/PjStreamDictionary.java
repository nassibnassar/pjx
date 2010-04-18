package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF stream dictionary.  It is normally used
   in constructing a PjStream object.
   @author Nassib Nassar
*/
public class PjStreamDictionary
	extends PjDictionary {

	/**
	   Creates a new stream dictionary.
	*/
	public PjStreamDictionary() {
		super();
	}

	/**
	   Creates a stream dictionary as a wrapper around an Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjStreamDictionary(Hashtable h) {
		super(h);
	}

	public void setLength(PjNumber length) {
		_h.put(PjName.LENGTH, length);
	}

	public void setLength(PjReference length) {
		_h.put(PjName.LENGTH, length);
	}

	public PjObject getLength() throws InvalidPdfObjectException {
		return hget(PjName.LENGTH);
	}

	public void setFilter(PjName filter) {
		_h.put(PjName.FILTER, filter);
	}

	public void setFilter(PjArray filter) {
		_h.put(PjName.FILTER, filter);
	}

	public void setFilter(PjReference filter) {
		_h.put(PjName.FILTER, filter);
	}

	public PjObject getFilter() throws InvalidPdfObjectException {
		return hget(PjName.FILTER);
	}

	public void setDecodeParms(PjObject decodeParms) {
		_h.put(PjName.DECODEPARMS, decodeParms);
	}

	public PjObject getDecodeParms() throws InvalidPdfObjectException {
		return hget(PjName.DECODEPARMS);
	}

	// we need something like this that takes a PjFileSpec instead of PjString
	/*
	public void setF(PjString f) {
		_h.put(PjName.F, f);
	}
	*/

	public PjObject getF() throws InvalidPdfObjectException {
		return hget(PjName.F);
	}

	public void setFFilter(PjName fFilter) {
		_h.put(PjName.FFILTER, fFilter);
	}

	public void setFFilter(PjArray fFilter) {
		_h.put(PjName.FFILTER, fFilter);
	}

	public void setFFilter(PjReference fFilter) {
		_h.put(PjName.FFILTER, fFilter);
	}

	public PjObject getFFilter() throws InvalidPdfObjectException {
		return hget(PjName.FFILTER);
	}

	public void setFDecodeParms(PjObject fDecodeParms) {
		_h.put(PjName.FDECODEPARMS, fDecodeParms);
	}

	public PjObject getFDecodeParms() throws InvalidPdfObjectException {
		return hget(PjName.FDECODEPARMS);
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjStreamDictionary(cloneHt());
	}
	
}
