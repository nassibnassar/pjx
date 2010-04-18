package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Examines a specified page dictionary and returns the set of all
   objects it references that are required for the page to be
   extracted from the document.  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfPageObjects extends PdfReferencedObjects {

	/**
	   The manager to use for resolving references.
	 */
	protected PdfManager _mgr;
	
	/**
	   Constructs a <code>PdfPageObjects</code> instance.
	   @param manager the manager associated with the document.
	 */
	public PdfPageObjects(PdfManager manager) {
		super(manager);
		_mgr = manager;
	}

	/**
	   Returns the set of all objects referenced by the specified
	   page object that are required for the page to be extracted
	   from the document.
	   @param obj the object to examine.
	   @throws PdfFormatException
	 */
	public Set getReferenced(PdfObject obj) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_mgr) {

				return super.getReferenced(obj);

			}
		}
	}

	/**
           A <code>PdfName</code> object representing the name
           <code>Annot</code>.
	*/
	protected static final PdfName PDFNAME_ANNOT = new PdfName("Annot");

	/**
           A <code>PdfName</code> object representing the name
           <code>P</code>.
	*/
	protected static final PdfName PDFNAME_P = new PdfName("P");

	/**
           A <code>PdfName</code> object representing the name
           <code>Page</code>.
	*/
	protected static final PdfName PDFNAME_PAGE = new PdfName("Page");

	/**
           A <code>PdfName</code> object representing the name
           <code>Parent</code>.
	*/
	protected static final PdfName PDFNAME_PARENT = new PdfName("Parent");

	/**
           A <code>PdfName</code> object representing the name
           <code>Type</code>.
	*/
	protected static final PdfName PDFNAME_TYPE = new PdfName("Type");

	/**
	   This method is used by {@link #getReferenced(PdfObject)
	   getReferenced(PdfObject)} and <b>should not be called
	   externally</b>.  (It is not synchronized.)
	   @param obj the object to filter.
	   @return the filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject preFilter(PdfObject obj) throws PdfFormatException {

		// check if it is a dictionary
		if (obj instanceof PdfDictionary) {

			Map map = ((PdfDictionary)obj).getMap();
			
			// check if it is a page dictionary
			Object j = map.get(PDFNAME_TYPE);
			if ( ( PdfNull.isNull(j) == false ) &&
			     ( j instanceof PdfName ) &&
			     ( ((PdfName)j).equals(PDFNAME_PAGE) ) ) {

				Map newMap = new HashMap(map);
				newMap.remove(PDFNAME_PARENT);
				return new PdfDictionary(newMap);
			}

			// check if it is an annotation dictionary
			j = map.get(PDFNAME_TYPE);
			if ( ( PdfNull.isNull(j) == false ) &&
			     ( j instanceof PdfName ) &&
			     ( ((PdfName)j).equals(PDFNAME_ANNOT) ) ) {

				Map newMap = new HashMap(map);
				newMap.remove(PDFNAME_P);
				return new PdfDictionary(newMap);
			}

		}
		
		return obj;
	}
	
}
