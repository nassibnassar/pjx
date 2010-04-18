package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;
import com.etymon.pj.object.pagemark.*;
import com.etymon.pjx.*;

/**
   Provides methods for retrieving and modifying various elements of a
   PDF document.
   @author Nassib Nassar
   @deprecated
*/
public class PdfModifier {

	/**
	   Returns the number of pages in the document.
	   @return the number of pages.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated Use {@link PdfPageTree#getNumberOfPages()
	   PdfPageTree.getNumberOfPages()}.
	 */
	public int getPageCount() throws IOException, com.etymon.pjx.PdfFormatException {
		return new PdfPageTree(_m).getNumberOfPages();
	}

	/**
           A <code>PdfName</code> object representing the name
           <code>AcroForm</code>.
	*/
	protected static final PdfName PDFNAME_ACROFORM = new PdfName("AcroForm");

	/**
           A <code>PdfName</code> object representing the name
           <code>Fields</code>.
	*/
	protected static final PdfName PDFNAME_FIELDS = new PdfName("Fields");

	/**
           A <code>PdfName</code> object representing the name
           <code>Kids</code>.
	*/
	protected static final PdfName PDFNAME_KIDS = new PdfName("Kids");

	/**
           A <code>PdfName</code> object representing the name
           <code>Pages</code>.
	*/
	protected static final PdfName PDFNAME_PAGES = new PdfName("Pages");

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
	   Constructs a <code>PdfModifier</code> instance based on a
	   specified <code>PdfManager</code>.
	 */
	public PdfModifier(PdfManager manager) {

		_m = manager;
		
	}

	/**
	   Retrieves an indirect reference to the document's catalog.
	   @return the indirect reference.
	   @throws PdfFormatException
	   @deprecated Use {@link PdfCatalog#getCatalog() PdfCatalog.getCatalog()}.
	 */
	public PdfReference getCatalogReference() throws com.etymon.pjx.PdfFormatException {
		synchronized (this) {

			PdfDictionary trailer = _m.getTrailerDictionary();
			
			Map map = trailer.getMap();
			
			Object root = map.get(PdfModifier.PDFNAME_ROOT);
			
			if ( !(root instanceof PdfReference) ) {
				throw new com.etymon.pjx.PdfFormatException("Catalog dictionary is not an indirect reference.");
			}
			
			return (PdfReference)root;
		}
	}
	
	/**
	   Retrieves the document's catalog.
	   @return the catalog object.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated Use {@link PdfCatalog#getCatalog() PdfCatalog.getCatalog()}.
	 */
	public PdfDictionary getCatalog() throws IOException, com.etymon.pjx.PdfFormatException {
		synchronized (this) {

			PdfReference catalogRef = getCatalogReference();

			if (catalogRef == null) {
				return null;
			}
			
			Object catalog = _m.getObjectIndirect(catalogRef);

			if ( !(catalog instanceof PdfDictionary) ) {
				throw new com.etymon.pjx.PdfFormatException("Catalog is not a dictionary.");
			}

			return (PdfDictionary)catalog;
		}
	}

	/**
	   Sets the document's catalog to a specified value.
	   @param catalog the new catalog.
	   @throws PdfFormatException
	   @deprecated Use {@link PdfManager#setObject(PdfObject, int)
	   PdfManager.setObject(PdfObject, int)}.
	 */
	public void setCatalog(PdfDictionary catalog) throws com.etymon.pjx.PdfFormatException {
		synchronized (this) {
		
			PdfReference catalogRef = getCatalogReference();

			if (catalogRef != null) {
				
				_m.setObject(catalog, catalogRef.getObjectNumber());
					     
			} else {

				// add catalog as a new indirect object
				int catalogId = _m.addObject(catalog);

				// add reference to catalog in file
				// trailer dictionary
				
				PdfDictionary trailer = _m.getTrailerDictionary();
			
				Map map = trailer.getMap();

				HashMap h = new HashMap(map.size());
				h.putAll(map);

				h.put(PdfModifier.PDFNAME_ROOT,
					 new PdfReference(catalogId, 0) );

				_m.setTrailerDictionary(new PdfDictionary(h));
			}

		}
	}

	/**
	   Retrieves an indirect reference to the root node of the
	   document's page tree.
	   @return the indirect reference.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated Use {@link PdfPageTree#getRoot()
	   PdfPageTree.getRoot()}.
	 */
	public PdfReference getPageTreeRootReference() throws IOException, com.etymon.pjx.PdfFormatException {
		synchronized (this) {

			PdfDictionary catalog = getCatalog();

			Map map = catalog.getMap();
			
			Object pages = map.get(PdfModifier.PDFNAME_PAGES);
			
			if ( !(pages instanceof PdfReference) ) {
				throw new com.etymon.pjx.PdfFormatException("Page tree root (Pages) is not an indirect reference.");
			}
			
			return (PdfReference)pages;
		}
	}

	/**
	   Retrieves the root node of the document's page tree.
	   @return the root node object.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated Use {@link PdfPageTree#getRoot() PdfPageTree.getRoot()}.
	 */
	public PdfDictionary getPageTreeRoot() throws IOException, com.etymon.pjx.PdfFormatException {
		synchronized (this) {

			PdfReference pageTreeRootRef = getPageTreeRootReference();

			if (pageTreeRootRef == null) {
				return null;
			}
			
			Object pageTreeRoot = _m.getObjectIndirect(pageTreeRootRef);

			if ( !(pageTreeRoot instanceof PdfDictionary) ) {
				throw new com.etymon.pjx.PdfFormatException("Page tree root is not a dictionary.");
			}

			return (PdfDictionary)pageTreeRoot;
		}
	}

	/**
	   Sets the root node of the document's page tree to a
	   specified value.
	   @param pageTreeNode the new root node.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated Use {@link PdfManager#setObject(PdfObject, int)
	   PdfManager.setObject(PdfObject, int)}.
	 */
	public void setPageTreeRoot(PdfDictionary pageTreeRoot) throws IOException, com.etymon.pjx.PdfFormatException {
		synchronized (this) {
		
			PdfReference pageTreeRootRef = getPageTreeRootReference();

			if (pageTreeRootRef != null) {
				
				_m.setObject(pageTreeRoot, pageTreeRootRef.getObjectNumber());
					     
			} else {

				// add page tree root as a new indirect object
				int pageTreeRootId = _m.addObject(pageTreeRoot);

				// add reference to page tree root in catalog
				
				PdfDictionary catalog = getCatalog();
			
				Map map = catalog.getMap();

				HashMap h = new HashMap(map.size());
				h.putAll(map);

				h.put(PdfModifier.PDFNAME_PAGES,
					 new PdfReference(pageTreeRootId, 0) );

				setCatalog(new PdfDictionary(h));
			}

		}
	}

	/**
	   @deprecated
	 */
	private void getFieldsAddField(ArrayList fieldList, PdfReference fieldRef)
		throws IOException, com.etymon.pjx.PdfFormatException {

		// resolve field reference
		PdfDictionary field;
		try {
			field = (PdfDictionary)(_m.getObjectIndirect(fieldRef));
		}
		catch (ClassCastException e) {
			throw new com.etymon.pjx.PdfFormatException("Field object is not a dictionary.");
		}

		Map fieldHt = field.getMap();

		// add the field to the list
		fieldList.add(field);
		
		// check if there are any kids
		PdfArray kids;
		try {
			kids = (PdfArray)(_m.getObjectIndirect((PdfObject)(fieldHt.get(PDFNAME_KIDS))));
		}
		catch (ClassCastException e) {
			throw new com.etymon.pjx.PdfFormatException("Kids object is not an array.");
		}
		
		// if there are kids, descend the tree
		if (kids != null) {
			List kidsV = kids.getList();
			int kidsV_n = kidsV.size();
			for (int x = 0; x < kidsV_n; x++) {

				// get the field object
				PdfReference fieldRef2;
				try {
					fieldRef2 = (PdfReference)(kidsV.get(x));
				}
				catch (ClassCastException e) {
					throw new com.etymon.pjx.PdfFormatException("Kids array element is not a reference.");
				}
				
				getFieldsAddField(fieldList, fieldRef2);

			}
		}
		
	}

	/**
	   This method is provided for compatibility with PJ.  It will
	   be transitioned toward a dedicated field class.
	   @throws PdfFormatException
	   @deprecated
	 */
	public PdfDictionary pjUpdateFieldValue(PdfDictionary origField, PdfDictionary field, String value)
		throws IOException, com.etymon.pjx.PdfFormatException {

		synchronized (this) {

			try {

				Map origFieldHt = new HashMap(origField.getMap());
				
				Map fieldHt = field.getMap();
				
				// store old value for use in search/replace within appeareances stream(s)
				PdfString oldValue = (PdfString)(fieldHt.get(new PdfName("V")));
				
				PdfString valueString = new PdfString(value);
				origFieldHt.put(new PdfName("V"), valueString);
				origFieldHt.put(new PdfName("DV"), valueString);
				
				// determine quadding
				PdfInteger q = (PdfInteger)(_m.getObjectIndirect((PdfObject)(fieldHt.get(new PdfName("Q")))));
				boolean leftJustified = false;
				boolean centered = false;
				boolean rightJustified = false;
				if (q == null) {
					leftJustified = true;
				} else {
					switch (q.getInt()) {
					case 1:
						centered = true;
						break;
					case 2:
						rightJustified = true;
						break;
					default:
						leftJustified = true;
					}
				}
				
				PdfDictionary ap =
					(PdfDictionary)(_m.getObjectIndirect((PdfObject)(fieldHt.get(new PdfName("AP")))));
				Map apHt;
				if (ap == null) {
					apHt = null;
				} else {
					apHt = new HashMap(ap.getMap());
					PdfObject apnObj = (PdfObject)(apHt.get(new PdfName("N")));
					int apnId;
					PdfReference apnRef;
					PdfObject apn;
					PdfDictionary apnDict;
					byte[] apnBuffer;
					if (apnObj instanceof PdfReference) {
						// it's an indirect object
						apnRef = (PdfReference)apnObj;
						apnId = apnRef.getObjectNumber();
						apn = _m.getObjectIndirect(apnRef);
					} else {
						// if it's not an indirect object, let's make it indirect
						apnId = _m.addObject(apnObj);
						apnRef = new PdfReference(apnId, 0);
						apHt.put(new PdfName("N"), apnRef);
						apn = apnObj;
					}
					
					// "/C" = center text
					// this assumes Courier 10 pt; we can add support
					// for others if needed.
					// it also assumes a page width of 8.5"; this also could
					// be adjusted or read from the document.
					
					float rectX1 = 0;
					float rectX2 = 0;
					float rectWidth = 0;
					if (centered) {
						// adjust RECT
						PdfArray rect =
							(PdfArray)(fieldHt.get(new PdfName("Rect")));
						List rectList = rect.getList();
						rectX1 = ((PdfNumber)rectList.get(0)).getFloat();
						rectX2 = ((PdfNumber)rectList.get(2)).getFloat();
						rectWidth = rectX2 - rectX1;
					}
					
					if ( (apn != null) && (apn instanceof PdfStream) ) {
						// if centered: remove any text matrix adjustments.
						// get page mark operators
						PjStream apnPj = (PjStream)PjxConvert.toPjObject(apn);
						Vector pmVector = new StreamParser().parse(
							apnPj.flateDecompress());
						if (oldValue != null) {
							replaceTextData(pmVector, oldValue, valueString);
						}
						if (centered) {
							adjustTextMatrixX(pmVector, rectWidth);
						}
						// reconstruct stream from modified pmVector
						ByteArrayOutputStream baos =
							new ByteArrayOutputStream();
						for (int pmX = 0; pmX < pmVector.size(); pmX++) {
							PageMark pm = (PageMark)(pmVector.elementAt(pmX));
							try {
								pm.writePdf(baos);
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						}
						byte[] ba = baos.toByteArray();
						PjStream temp = new PjStream( apnPj.getStreamDictionary(), ba );
						_m.setObject(PjxConvert.toPjxObject(temp), apnId);
					}
				}

				if (apHt != null) {
					origFieldHt.put(new PdfName("AP"), new PdfDictionary(apHt));
				}
				
				return new PdfDictionary(origFieldHt);
				
			}
			catch (com.etymon.pj.exception.PdfFormatException e) {
				throw new com.etymon.pjx.PdfFormatException(e.getMessage());
			}
			catch (com.etymon.pj.exception.InvalidPdfObjectException f) {
				throw new com.etymon.pjx.PdfFormatException(f.getMessage());
			}
		}
	}

	// used exclusively by updateFieldValue()
	/**
	   @deprecated
	 */
	private static void replaceTextData(Vector pmVector, PdfString oldText, PdfString newText)
		throws com.etymon.pj.exception.PdfFormatException {
		
		// this method replaces text data oldS with newS

		int pmX = pmVector.size();

		PjString oldTextPj = (PjString)PjxConvert.toPjObject(oldText);
		PjString newTextPj = (PjString)PjxConvert.toPjObject(newText);

		// no particular reason for searching backwards; just
		// because this was adapted from clearTextMatrixX()
		while (pmX > 0) {
			
			pmX--;
			PageMark pm = (PageMark)(pmVector.elementAt(pmX));
			
			if (pm instanceof XTj) {
				XTj tj = (XTj)pm;
				if (tj.getText().equals(oldTextPj)) {
					XTj newTj = new XTj(newTextPj);
					pmVector.setElementAt(newTj, pmX);
				}
			}
			
		}
	}
	
	// used exclusively by updateFieldValue()
	/**
	   @deprecated
	 */
	private static void adjustTextMatrixX(Vector pmVector, float rectWidth) {
		// this method examines the last text matrix in
		// pmVector and sets the X matrix value in order to
		// center the text written by the subsequent Tj
		// operator.

		int pmX = pmVector.size();
		float textWidth = 0;
		float rectCenter = rectWidth / 2;
		
		while (pmX > 0) {
			
			pmX--;
			PageMark pm = (PageMark)(pmVector.elementAt(pmX));
			
			if (pm instanceof XTj) {
				XTj tj = (XTj)pm;
				textWidth = tj.getText().getString().length() * 6;
			}
			
			if (pm instanceof XTm) {
				float newX = rectCenter - (textWidth / 2);
				if (newX < 0) {
					newX = 0;
				}
				XTm tm = (XTm)pm;
				XTm newTm = new XTm(
					tm.getA(),
					tm.getB(),
					tm.getC(),
					tm.getD(),
					new PjNumber(newX),
					tm.getY());
				pmVector.setElementAt(newTm, pmX);
				pmX = 0;  // Tm found, now we can stop
			}
			
		}
	}
	
	// used exclusively by updateFieldValue()
	/**
	   @deprecated
	 */
	private static void clearTextMatrixX(Vector pmVector) {
		// this method examines the last text matrix in
		// pmVector and sets the X matrix value to 0.

		int pmX = pmVector.size();

		while (pmX > 0) {
			
			pmX--;
			PageMark pm = (PageMark)(pmVector.elementAt(pmX));
			
			if (pm instanceof XTm) {
				XTm tm = (XTm)pm;
				XTm newTm = new XTm(
					tm.getA(),
					tm.getB(),
					tm.getC(),
					tm.getD(),
					PjNumber.ZERO,
					tm.getY());
				pmVector.setElementAt(newTm, pmX);
				pmX = 0;  // Tm found, now we can stop
			}
			
		}
	}

	// used exclusively by inheritFieldAttributes()
	/**
	   @deprecated
	 */
	private void inheritFieldAttributesCollapse(PjName name, Hashtable ht, PjDictionary newNode, PjDictionary parent) {
		if (ht.get(name) == null) {
			Object obj = parent.getHashtable().get(name);
			if (obj != null) {
				ht.put(name, obj);
			}
		}
	}
	
}
