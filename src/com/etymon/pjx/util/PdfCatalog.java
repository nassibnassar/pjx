package com.etymon.pjx.util;

//import java.io.*;
//import java.util.*;
import com.etymon.pjx.*;

/**
   Provides methods related to a PDF document's catalog dictionary.
   @author Nassib Nassar
*/
public class PdfCatalog {

	/**
           A <code>PdfName</code> object representing the name
           <code>Root</code>.
	*/
	protected static final PdfName PDFNAME_ROOT = new PdfName("Root");

	/**
	   The manager associated with this document.
	*/
	protected PdfManager _m;

	/**
	   Constructs a <code>PdfCatalog</code> instance based on a
	   specified <code>PdfManager</code>.
	 */
	public PdfCatalog(PdfManager manager) {

		_m = manager;
		
	}

	/**
	   Retrieves an indirect reference to the document's catalog.
	   @return the indirect reference.
	   @throws PdfFormatException
	 */
	public PdfReference getCatalog() throws PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				PdfDictionary trailer = _m.getTrailerDictionary();
				
				Object obj = trailer.getMap().get(PDFNAME_ROOT);
				
				if ( !(obj instanceof PdfReference) ) {
					throw new PdfFormatException("Catalog dictionary is not an indirect reference.");
				}
				
				return (PdfReference)obj;

			}
		}
	}
	
}
