package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of a dictionary node in a PDF Pages tree (abstract base class).
   @author Nassib Nassar
*/
public abstract class PjPagesNode
	extends PjDictionary {

	/**
	   Creates a new Pages dictionary node.
	*/
	public PjPagesNode() {
		super();
	}

	/**
	   Creates a Pages dictionary node as a wrapper around a Hashtable.
	   @param h the Hashtable to use for this dictionary node.
	*/
	public PjPagesNode(Hashtable h) {
		super(h);
	}

	public void setParent(PjReference parent) {
		_h.put(PjName.PARENT, parent);
	}

	public PjReference getParent() throws InvalidPdfObjectException {
		return hgetReference(PjName.PARENT);
	}

	public void setMediaBox(PjRectangle mediaBox) {
		_h.put(PjName.MEDIABOX, mediaBox);
	}

	public void setMediaBox(PjReference mediaBox) {
		_h.put(PjName.MEDIABOX, mediaBox);
	}

	public PjObject getMediaBox() throws InvalidPdfObjectException {
		return hget(PjName.MEDIABOX);
	}

	public void setResources(PjResources resources) {
		_h.put(PjName.RESOURCES, resources);
	}

	public void setResources(PjReference resources) {
		_h.put(PjName.RESOURCES, resources);
	}

	public PjObject getResources() throws InvalidPdfObjectException {
		return hget(PjName.RESOURCES);
	}

	public void setCropBox(PjRectangle cropBox) {
		_h.put(PjName.CROPBOX, cropBox);
	}

	public void setCropBox(PjReference cropBox) {
		_h.put(PjName.CROPBOX, cropBox);
	}

	public PjObject getCropBox() throws InvalidPdfObjectException {
		return hget(PjName.CROPBOX);
	}

	public void setRotate(PjNumber rotate) {
		_h.put(PjName.ROTATE, rotate);
	}

	public void setRotate(PjReference rotate) {
		_h.put(PjName.ROTATE, rotate);
	}

	public PjObject getRotate() throws InvalidPdfObjectException {
		return hget(PjName.ROTATE);
	}

	public void setDur(PjNumber dur) {
		_h.put(PjName.DUR, dur);
	}

	public void setDur(PjReference dur) {
		_h.put(PjName.DUR, dur);
	}

	public PjObject getDur() throws InvalidPdfObjectException {
		return hget(PjName.DUR);
	}

	public void setHid(PjBoolean hid) {
		_h.put(PjName.HID, hid);
	}

	public void setHid(PjReference hid) {
		_h.put(PjName.HID, hid);
	}

	public PjObject getHid() throws InvalidPdfObjectException {
		return hget(PjName.HID);
	}

	public void setTrans(PjDictionary trans) {
		_h.put(PjName.TRANS, trans);
	}

	public void setTrans(PjReference trans) {
		_h.put(PjName.TRANS, trans);
	}

	public PjObject getTrans() throws InvalidPdfObjectException {
		return hget(PjName.TRANS);
	}

	public void setAA(PjDictionary aA) {
		_h.put(PjName.AA, aA);
	}

	public void setAA(PjReference aA) {
		_h.put(PjName.AA, aA);
	}

	public PjObject getAA() throws InvalidPdfObjectException {
		return hget(PjName.AA);
	}

}
