/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

/**
   A filtering function that allows examining and modifying PDF
   objects recursively.  {@link PdfObject#filter(PdfObjectFilter)
   PdfObject.filter(PdfObjectFilter)} passes each object through
   {@link #preFilter(PdfObject) preFilter(PdfObject)} before
   descending recursively into the object's contents (if it is a
   container such as {@link PdfArray PdfArray} or {@link PdfDictionary
   PdfDictionary}).  It then passes the (possibly modified) object
   through {@link #postFilter(PdfObject) postFilter(PdfObject)} for a
   second opportunity to examine or modify the object.  The modified
   object is not required to be of the same type as the original
   object.
   @author Nassib Nassar
*/
public interface PdfObjectFilter {

	/**
	   Examines a PDF object and optionally returns a modified
	   object.  This method should return the original object or a
	   modified replacement.  If no modifications are desired, it
	   should return the original object.  This method may also
	   return <code>null</code> in order to discard the object;
	   however, this will cause {@link #postFilter(PdfObject)
	   postFilter(PdfObject)} to be called with a
	   <code>null</code> value as its parameter.  A {@link
	   PdfArray PdfArray} or {@link PdfDictionary PdfDictionary}
	   object is considered to be a container, and this method
	   filters the container as a whole before filtering each
	   element within it (assuming the elements are still present
	   in the modified object).  When this method receives a
	   container, that container's contents will not yet have been
	   filtered by this method.
	   @param obj the object to examine.
	   @return the "filtered" object.
	   @throws PdfFormatException
	 */
	public PdfObject preFilter(PdfObject obj) throws PdfFormatException;

	/**
	   Examines a PDF object and optionally returns a modified
	   object.  This method should return the original object or a
	   modified replacement.  If no modifications are desired, it
	   should return the original object.  This method may also
	   return <code>null</code> in order to discard the object.  A
	   {@link PdfArray PdfArray} or {@link PdfDictionary
	   PdfDictionary} object is considered to be a container, and
	   this method filters each element within it before filtering
	   the container as a whole.  When this method receives a
	   container, that container's contents will have already been
	   filtered by this method.
	   @param obj the object to examine.
	   @return the "filtered" object.
	   @throws PdfFormatException
	 */
	public PdfObject postFilter(PdfObject obj) throws PdfFormatException;

}
