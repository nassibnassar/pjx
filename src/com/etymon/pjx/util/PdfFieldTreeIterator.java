/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   An iterator over the tree of field dictionaries in a PDF document.
   @author Nassib Nassar
*/
public interface PdfFieldTreeIterator {

	/**
	   Returns <code>true</code> if any more field objects remain.
	   @return <code>true</code> if more field objects remain.
	*/
	public boolean hasNext() throws PdfFormatException;

	/**
	   Returns an indirect reference to the next field object.
	   @return the indirect reference.
	*/
	public PdfReference next() throws NoSuchElementException, IOException, PdfFormatException;

}
