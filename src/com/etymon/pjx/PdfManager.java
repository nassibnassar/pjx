/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.util.*;

/**
   Manages the modification or creation of a PDF document.  This class
   is synchronized.
   @author Nassib Nassar
*/
public final class PdfManager {

	protected long _last_startxref;

	/**
	   @deprecated Do not use this method.
	 */
	public long getStartxref() {
		return _last_startxref;
	}
	
	protected static final PdfName PDFNAME_PREV = new PdfName("Prev");

	protected static final PdfName PDFNAME_SIZE = new PdfName("Size");

	/**
	   The collection of PDF objects that have been modified.  Each
	   PDF object is keyed by its associated object number (stored
	   as type Integer).
	 */
	protected Map _modobj;

	/**
	   The total size of the cross-reference table, taking
	   together both the PDF input document and the modified
	   objects.  This is also used as the next free object number.
	 */
	protected int _modsize;

	/**
	   The PDF input document.
	 */
	protected PdfReader _reader;
	
	/**
	   Returns the <code>PdfReader</code> instance associated with
	   this document.
	*/
	public PdfReader getReader() {
		synchronized (this) {

			return _reader;

		}
	}
	
	/**
	   The beginning offset of the cross-reference table in the
	   PDF input document.
	 */
	protected long _startxref;

	/**
	   The document's trailer dictionary, in its current (possibly
	   modified) state.
	 */
	protected PdfDictionary _trailer;

	/**
	   The cross-reference table and associated trailer read from
	   the PDF input document.
	 */
	protected XrefTable _xt;

	/**
           Constructs a <code>PdfManager</code> representing an empty
           PDF document.  The document in this initial state contains
           no objects and therefore does not correspond to a valid PDF
           file.
	   @param init if false, specifies that the document contain
	   no objects (and therefore will not correspond to a valid
	   PDF file); if true, that the document be initialized with
	   a minimal set of objects to create a single blank page.
        */
        public PdfManager(boolean init) {

		_reader = null;
		_startxref = 0;
		_xt = null;

		_trailer = new PdfDictionary(new HashMap());
		
		_modsize = 1;
		construct();

		if (init) {
			initialize();
		}
	}

	/**
	   Creates the minimal set of objects in a newly constructed
	   document in order to create a single blank page.  This
	   method assumes that the document contains no objects.
	   (This method is adapted from
	   com.etymon.pj.Pdf.createEmpty().)
        */
	private void initialize() {

		List list;
		Map map;
		PdfName nameType = new PdfName("Type");

		// Make a ProcSet.
		list = new ArrayList(2);
		list.add(new PdfName("PDF"));
		list.add(new PdfName("Text"));
		PdfArray procSet = new PdfArray(list);
		int procSetId = addObject(procSet);

		// Make a Resources dictionary.
		map = new HashMap(1);
		map.put(new PdfName("ProcSet"),
			new PdfReference(procSetId, 0));
		PdfDictionary resources = new PdfDictionary(map);
		int resourcesId = addObject(resources);

		// Make a MediaBox.
		list = new ArrayList(4);
		list.add(new PdfInteger(0));
		list.add(new PdfInteger(0));
		list.add(new PdfInteger(612));
		list.add(new PdfInteger(792));
		PdfArray mediaBox = new PdfArray(list);

		// Make a Page placeholder.
		int pageId = addObject(new PdfDictionary());

		// Make a Kids array.
		list = new ArrayList(1);
		list.add(new PdfReference(pageId, 0));
		PdfArray kids = new PdfArray(list);

		// Make the root Pages node.
		map = new HashMap(5);
		map.put(nameType, new PdfName("Pages"));
		map.put(new PdfName("Resources"), new PdfReference(resourcesId, 0));
		map.put(new PdfName("MediaBox"), mediaBox);
		map.put(new PdfName("Count"), new PdfInteger(1));
		map.put(new PdfName("Kids"), kids);
		int rootId = addObject(new PdfDictionary(map));

		// Go back and recreate the Page correctly, including
		// the Parent pointer.
		map = new HashMap(2);
		map.put(nameType, new PdfName("Page"));
		map.put(new PdfName("Parent"),
			new PdfReference(rootId, 0));
		PdfDictionary page = new PdfDictionary(map);
		setObject(page, pageId);

		// Make the Catalog.
		map = new HashMap(2);
		map.put(nameType, new PdfName("Catalog"));
		map.put(new PdfName("Pages"),
			new PdfReference(rootId, 0));
		int catalogId = addObject(new PdfDictionary(map));

		// Create Info dictionary with default fields. ?
		
		// Update trailer dictionary.
		map = new HashMap(getTrailerDictionary().getMap());
		map.put(new PdfName("Root"),
			new PdfReference(catalogId, 0));
		setTrailerDictionary(new PdfDictionary(map));
		
	}
	
	/**
           Constructs a <code>PdfManager</code> representing an
           existing PDF document.
           @param pdfReader specifies the existing PDF document to
           read.
	   @throws IOException
	   @throws PdfFormatException
        */
        public PdfManager(PdfReader pdfReader) throws IOException, PdfFormatException {

		_startxref = pdfReader.readStartxref();
		_xt = pdfReader.readXrefTable(_startxref);
		_trailer = _xt.getTrailerDictionary();
		_reader = pdfReader;
		_modsize = _xt.size();
		construct();
	}

	/**
	   Adds a PDF object to the document and assigns a new object
	   number to it.
	   @param obj the PDF object to be added.
	   @return the new object number assigned to the PDF object.
	   The generation number of the object will be 0.
	 */
	public int addObject(PdfObject obj) {
		synchronized (this) {

			int id = _modsize++;
			
			setObject(obj, id);

			return id;
		}
	}

	/**
	   Retrieves the PDF object associated with a specified object
	   number.
	   @param objectNumber specifies the object number of the PDF
	   object to be retrieved.
	   @return the retrieved PDF object.
	   @throws IOException
	   @throws PdfFormatException
	*/
	public PdfObject getObject(int objectNumber) throws IOException, PdfFormatException {
		synchronized (this) {
			
			// first check hashmap
			PdfObject obj = (PdfObject)_modobj.get(new Integer(objectNumber));
			if (obj != null) {
				return obj;
			}
			
			// next try the xref
			if (_reader == null) {
				return null;
			}
			if (_xt.unwrapUsageArray()[objectNumber] != XrefTable.ENTRY_IN_USE) {
				return null;
			}
			long start = _xt.getIndex(objectNumber);
			long end = _xt.estimateObjectEnd(objectNumber);
			return _reader.readObject(start, end, true, _xt);
		}
		
	}

	/**
	   Retrieves the PDF object referred to by a specified
	   indirect reference object.  If the specified object is not
	   an indirect reference but a direct object, that object
	   itself is returned.  If the specified object is
	   <code>null</code>, then <code>null</code> is returned.
	   @param obj the indirect reference object (or direct object,
	   or <code>null</code>).
	   @return the retrieved object (or direct object).
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfObject getObjectIndirect(PdfObject obj) throws IOException, PdfFormatException {
		synchronized (this) {

			if (obj == null) {
				return null;
			}
			
			if (obj instanceof PdfReference) {
				// this should be changed from
				// recursive to iterative, for best
				// efficiency
				return getObjectIndirect( getObject( ((PdfReference)obj).getObjectNumber() ) );
			} else {
				return obj;
			}
			
		}
	}

	/**
	   Returns the document's trailer dictionary.
	   @return the trailer dictionary.
	 */
	public PdfDictionary getTrailerDictionary() {
		synchronized (this) {
			return _trailer;
		}
	}

	/**
	   Returns the current size of the cross-reference table,
	   taking into account any modifications that have been made
	   to the document via this PdfManager.
	   @return the size of the cross-reference table.
	 */
	public int getXrefTableSize() {
		synchronized (this) {
			return _modsize;
		}
	}

	/**
	   Performs initialization common to multiple constructors of
	   this class.  This method is only intended to be called from
	   the constructors.
	 */
	protected void construct() {
		_modobj = new HashMap();
	}

	/**
	   Adds a PDF object to the document and assigns the specified
	   object number to it.  If an object in the document is
	   already assigned to the specified object number, that
	   object is replaced with the new one.
	   @param obj the PDF object to be added.
	   @param objectNumber the object number to assign to the PDF
	   object.
	 */
	public void setObject(PdfObject obj, int objectNumber) {
		synchronized (this) {
			_modobj.put(new Integer(objectNumber), obj);
			if (objectNumber >= _modsize) {
				_modsize = objectNumber + 1;
			}
		}
	}

	/**
	   Assigns the document a new trailer dictionary, replacing
	   the existing one (if any).
	   @param dictionary specifies the new trailer dictionary.
	 */
	public void setTrailerDictionary(PdfDictionary dictionary) {
		synchronized (this) {
			_trailer = dictionary;
		}
	}

	/**
	   Writes the document in PDF format, including all
	   modifications made through this <code>PdfManager</code>.
	   This method is equivalent to using {@link
	   #writeDocument(PdfWriter, boolean) writeDocument(...,
	   true)}; i.e. it uses incremental update if applicable.
	   Note that this means the resultant document will be larger
	   than the original document; otherwise use {@link
	   #writeDocument(PdfWriter, boolean) writeDocument(...,
	   false)} to get a smaller resultant file, although it will
	   usually take longer to generate.  The
	   <code>PdfWriter</code> should be newly created (i.e. it
	   should not have been previously used for anything); and
	   after this method has been called, the
	   <code>PdfWriter</code> should be closed and discarded.
	   @param pdfWriter specifies the PDF document to write.
	   @return the number of bytes written.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public long writeDocument(PdfWriter pdfWriter) throws IOException, PdfFormatException {
		return writeDocument(pdfWriter, true);
	}
	
	/**
	   Writes the document in PDF format, including all
	   modifications made through this <code>PdfManager</code>.
	   This method will optionally use PDF's incremental update
	   format, which often takes significantly less processing
	   time but creates a larger resultant PDF file and may be
	   slower for a reader to open (if the file has been updated
	   many times in this way).  Incremental update should not be
	   used when the original document is a Linearized PDF file if
	   changes have been made that would invalidate its
	   correctness, unless the resultant PDF file will not be used
	   in an application that depends on its correct
	   Linearization.  If this <code>PdfManager</code> represents
	   a new document rather than modifying an existing one, then
	   the incremental update option is not applicable and is
	   disregarded.  The <code>PdfWriter</code> should be newly
	   created (i.e. it should not have been previously used for
	   anything); and after this method has been called, the
	   <code>PdfWriter</code> should be closed and discarded.
	   @param pdfWriter specifies the PDF document to write.
	   @param useIncrementalUpdate specifies whether incremental
	   update format should be used.  A value of <code>true</code>
	   enables incremental update.
	   @return the number of bytes written.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public long writeDocument(PdfWriter pdfWriter, boolean useIncrementalUpdate)
		throws IOException, PdfFormatException {
		synchronized (this) {
			
			int modsize = _modsize;
			XrefTable xt = _xt;
			PdfReader reader = _reader;
			Map modobj = _modobj;
			
			boolean incUpdate;
			if (reader == null) {
				incUpdate = false;
			} else {
				incUpdate = useIncrementalUpdate;
			}
			
			PdfWriter w = pdfWriter;
			long pos = 0;
			
			if (incUpdate) {
				pos += w.writeCopy(reader);
			} else {
				pos += w.writeHeader();
			}
			
			long[] nindex = new long[modsize];
			int[] ngeneration = new int[modsize];
			byte[] nusage = new byte[modsize];
			// (maybe we should ideally clone the
			// trailer instead of modifying the original)
			HashMap ntrailerMap = new HashMap(_trailer.getMap());
			
			long[] index = null;
			int[] generation = null;
			byte[] usage = null;

			if (reader != null) {
				index = xt.unwrapIndexArray();
				generation = xt.unwrapGenerationArray();
				usage = xt.unwrapUsageArray();
			}
			
			int id;
			// iterate through modified objects, adding
			// them to the new XrefTrailer
			for (Iterator it = modobj.keySet().iterator(); it.hasNext(); ) {
				Integer key = (Integer)it.next();
				id = key.intValue();
				nusage[id] = XrefTable.ENTRY_IN_USE;
				nindex[id] = pos;
				PdfObject obj = (PdfObject)modobj.get(key);
				
				// determine generation number
				if (reader == null) {
					ngeneration[id] = 0;
				} else {
					if ( (id < xt.size()) && (usage[id] != XrefTable.ENTRY_UNDEFINED) ) {
						ngeneration[id] = generation[id];
					} else {
						ngeneration[id] = 0;
					}
				}
				
				if ( (incUpdate) || (reader == null) ) {
					pos += w.writeObjectIndirect(obj, id, ngeneration[id]);
				}
			}
			
			// always keep entry 0
			ngeneration[0] = 65535;
			nusage[0] = XrefTable.ENTRY_FREE;
			
			if (!incUpdate) {
				for (id = 1; id < modsize; id++) {
					PdfObject obj = null;
					if ( (reader != null) && (nusage[id] == XrefTable.ENTRY_UNDEFINED) ) {
						if ( (id < xt.size()) &&
						     (usage[id] != XrefTable.ENTRY_UNDEFINED) ) {
							if (usage[id] == XrefTable.ENTRY_IN_USE) {
								long start = index[id];
								long end = xt.estimateObjectEnd(id);
								obj = reader.readObject(start, end, true, xt);
							}
							ngeneration[id] = generation[id];
							nusage[id] = usage[id];
						}
					} else {
						obj = (PdfObject)modobj.get(new Integer(id));
					}
					if (nusage[id] == XrefTable.ENTRY_IN_USE) {
						nindex[id] = pos;
						pos += w.writeObjectIndirect(obj, id, ngeneration[id]);
					}
					
				}
			}
			
			if (incUpdate) {
				ntrailerMap.put(PDFNAME_PREV, new PdfLong(_startxref));
			} else {
				ntrailerMap.remove(PDFNAME_PREV);
			}
			ntrailerMap.put(PDFNAME_SIZE, new PdfInteger(modsize));
			
			XrefTable nxt = XrefTable.wrap(nindex, ngeneration, nusage,
						       new PdfDictionary(ntrailerMap));
			
			_last_startxref = pos;
			
			pos += w.writeXrefTable(nxt, pos);

			return pos;
		}
	}
	
}
