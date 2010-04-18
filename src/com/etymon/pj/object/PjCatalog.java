package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a PDF Catalog dictionary.
   @author Nassib Nassar
*/
public class PjCatalog
	extends PjDictionary {

	/**
	   Creates a new Catalog dictionary.
	*/
	public PjCatalog() {
		super();
		_h.put(PjName.TYPE, PjName.CATALOG);
	}

	/**
	   Creates a Catalog dictionary as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary.
	*/
	public PjCatalog(Hashtable h) {
		super(h);
	}

	public void setPages(PjReference pages) {
		_h.put(PjName.PAGES, pages);
	}

	public PjObject getPages() throws InvalidPdfObjectException {
		return hget(PjName.PAGES);
	}

	public void setOutlines(PjReference outlines) {
		_h.put(PjName.OUTLINES, outlines);
	}

	public PjObject getOutlines() throws InvalidPdfObjectException {
		return hget(PjName.OUTLINES);
	}
	
	public void setPageMode(PjName pageMode) {
		_h.put(PjName.PAGEMODE, pageMode);
	}

	public void setPageMode(PjReference pageMode) {
		_h.put(PjName.PAGEMODE, pageMode);
	}

	public PjObject getPageMode() throws InvalidPdfObjectException {
		return hget(PjName.PAGEMODE);
	}

	public void setOpenAction(PjArray openAction) {
		_h.put(PjName.OPENACTION, openAction);
	}

	public void setOpenAction(PjDictionary openAction) {
		_h.put(PjName.OPENACTION, openAction);
	}

	public void setOpenAction(PjReference openAction) {
		_h.put(PjName.OPENACTION, openAction);
	}

	public PjObject getOpenAction() throws InvalidPdfObjectException {
		return hget(PjName.OPENACTION);
	}

	public void setThreads(PjReference threads) {
		_h.put(PjName.THREADS, threads);
	}

	public PjObject getThreads() throws InvalidPdfObjectException {
		return hget(PjName.THREADS);
	}

	public void setDests(PjReference dests) {
		_h.put(PjName.DESTS, dests);
	}

	public PjObject getDests() throws InvalidPdfObjectException {
		return hget(PjName.DESTS);
	}

	public void setNames(PjDictionary names) {
		_h.put(PjName.NAMES, names);
	}

	public void setNames(PjReference names) {
		_h.put(PjName.NAMES, names);
	}

	public PjObject getNames() throws InvalidPdfObjectException {
		return hget(PjName.NAMES);
	}

	public void setURI(PjDictionary uri) {
		_h.put(PjName.URI, uri);
	}

	public void setURI(PjReference uri) {
		_h.put(PjName.URI, uri);
	}

	public PjObject getURI() throws InvalidPdfObjectException {
		return hget(PjName.URI);
	}

	public void setAA(PjDictionary aa) {
		_h.put(PjName.URI, aa);
	}

	public void setAA(PjReference aa) {
		_h.put(PjName.URI, aa);
	}

	public PjObject getAA() throws InvalidPdfObjectException {
		return hget(PjName.AA);
	}

	public void setViewerPreferences(PjDictionary viewerPreferences) {
		_h.put(PjName.VIEWERPREFERENCES, viewerPreferences);
	}

	public void setViewerPreferences(PjReference viewerPreferences) {
		_h.put(PjName.VIEWERPREFERENCES, viewerPreferences);
	}

	public PjObject getViewerPreferences() throws InvalidPdfObjectException {
		return hget(PjName.VIEWERPREFERENCES);
	}

	public void setAcroForm(PjDictionary acroForm) {
		_h.put(PjName.ACROFORM, acroForm);
	}

	public void setAcroForm(PjReference acroForm) {
		_h.put(PjName.ACROFORM, acroForm);
	}

	public PjObject getAcroForm() throws InvalidPdfObjectException {
		return hget(PjName.ACROFORM);
	}

	/**
	   Examines a dictionary to see if it is a PDF Catalog object.
	   @param dictionary the dictionary to examine.
	   @return true if the dictionary could be interpreted as a
	   valid PjCatalog object.
	*/
	public static boolean isLike(PjDictionary dictionary) {
		Hashtable h = dictionary.getHashtable();
		// check if the Type is Catalog
		try {
			PjName type = (PjName)(h.get(PjName.TYPE));
			if (type == null) {
				return false;
			}
			if ( ! type.equals(PjName.CATALOG) ) {
				return false;
			}
		}
		catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjCatalog(cloneHt());
	}
	
}
