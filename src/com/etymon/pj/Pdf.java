package com.etymon.pj;

import java.io.*;
import java.util.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;
import com.etymon.pj.object.pagemark.*;
import com.etymon.pj.util.*;

/**
   A document representation of a PDF file.
   @author Nassib Nassar
 */
public class Pdf {

	/**
	   Creates an empty PDF document.
	*/
	public Pdf() {
		init();
		createEmpty();
	}

	/**
	   Creates a PDF document from an existing PDF file.
	   @param filename the name of the PDF file to read.
	   @exception IOException if an I/O error occurs.
	   @exception PjException if a PDF error occurs.
	*/
	public Pdf(String filename) throws IOException, PjException {

		readFromFile(filename);

		// set the Producer in the Info dictionary to pj
		// get the Info dictionary
		PjReference infoRef;
		try {
			infoRef = getInfoDictionary();
		}
		catch (InvalidPdfObjectException e) {
			infoRef = null;
		}
		PjInfo info;
		if (infoRef == null) {
			// create a new Info dictionary and add it
			info = new PjInfo();
			int infoId = registerObject(info);
			infoRef = new PjReference(new PjNumber(infoId));
			setInfoDictionary(infoRef);
		} else {
			PjDictionary d = (PjDictionary)(getObject(infoRef.getObjNumber().getInt()));
			info = new PjInfo(d.getHashtable());
		}
		// set the Producer field
		// PjInfo.setProducer(PjObject) automatically includes pj in the string
		info.setProducer(new PjString(""));
        }

	/**
	   Writes this PDF document to a file in PDF format.
	   @param filename the name of the PDF file to create.
	   @exception IOException if an I/O error occurs.
	 */
	public void writeToFile(String filename) throws IOException {
		File file = new File(filename);
		file.delete();
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		writeToStream(bos);
		bos.close();
		fos.close();
	}

	/**
	   Writes this PDF document to a stream in PDF format.
	   @param os the stream to write to.
	   @exception IOException if an I/O error occurs.
	 */
	public void writeToStream(OutputStream os) throws IOException {
		// first make sure to remove the Prev field from the
		// trailer if it is left over from having read a
		// multi-part xref!
		_trailer.remove(PjName.PREV);
		// remove the ID (if there is one) from the trailer
		_trailer.remove(PjName.ID);
		// ok, go ahead
		long z = 0;
		z = z + PjObject.writeln(os, "%PDF-" + PjConst.PDF_VERSION);
		z = z + PjObject.writeln(os, PjConst.VERSION_IN_PDF);
		// The pj copyright notice is inserted into all PDF
		// files output by pj; you may not remove this
		// copyright notice.
		z = z + PjObject.writeln(os, PjConst.COPYRIGHT_IN_PDF);
		z = z + PjObject.writeln(os, "%\323\343\317\342");
		PjObject obj;
		Integer objnum;
		int highest = 0;
		int size = _objects.size();
		long[] position = new long[size];
		for (int x = 1; x < size; x++) {
			if (x > highest) {
				highest = x;
			}
			obj = _objects.objectAt(x);
			position[x] = z;
			z = z + PjObject.writeln(os, x + " 0 obj");
			if (obj != null) {
				z = z + obj.writePdf(os);
			} else {
				// this is a small hack to avoid having to create "f" entries in the xref table
				z = z + PjNumber.ZERO.writePdf(os);
			}
			z = z + PjObject.writeln(os, "");
			z = z + PjObject.writeln(os, "endobj");
		}
		// write out xref
		long startxref = z;
		z = z + PjObject.writeln(os, "xref");
		int p = 0;
		int r;
		Long g;
		String s;
		position[0] = -1;
		int count = 0;
		while (p <= highest) {
			while ( (p <= highest) && (position[p] == 0) ) {
				p++;
			}
			r = p;
			while ( (r <= highest) && (position[r] != 0) ) {
				r++;
			}
			z = z + PjObject.write(os, p + " ");
			z = z + PjObject.writeln(os, new Integer(r - p));
			for (int x = p; x < r; x++) {
				count++;
				if (x == 0) {
					z = z + PjObject.write(os,
								 "0000000000 65535 f \n");
				} else {
					s = new Long(position[x]).toString();
					for (int w = 1;
					     (w + s.length()) <= 10; w++) {
						z = z + PjObject.write(os, "0");
					}
					z = z + PjObject.write(os, s);
					z = z + PjObject.write(os, " 00000 n \n");
				}
			}
			p = r;
		}
		// write out trailer
		z = z + PjObject.writeln(os, "trailer");
		_trailer.put(new PjName("Size"), new PjNumber(count));
		PjDictionary trailer = new PjDictionary(_trailer);
		z = z + trailer.writePdf(os);
		z = z + PjObject.writeln(os, "");
		z = z + PjObject.writeln(os, "startxref");
		z = z + PjObject.writeln(os, new Long(startxref));
		z = z + PjObject.writeln(os, "%%EOF");
	}

	/**
	   Registers a PjObject within this PDF document.
	   @param obj the PjObject to register.
	   @return the new object number of the registered PjObject.
	 */
	public int registerObject(PjObject obj) {
		int n = _objects.getFirstFree();
		_objects.setObjectAt(obj, n);
		return n;
	}

	/**
	   Registers a PjObject within this PDF document using a
	   specified object number.
	   @param obj the PjObject to register.
	   @param objectNumber the object number to register obj under.
	*/
	public void registerObject(PjObject obj, int objectNumber) {
		_objects.setObjectAt(obj, objectNumber);
	}

	/**
	   Adds a PjObject to a page in this PDF document.
	   @param page the page object to add to.
	   @param objectNumber the object number of the PjObject to add.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	 */
	public void addToPage(PjPage page, int objectNumber) throws InvalidPdfObjectException {
		PjReference objectToAdd = new PjReference(new PjNumber(objectNumber));
		// we handle four cases of /Contents:
		// 1) does not exist
		// 2) reference to a stream object
		// 3) reference to an array of references to stream objects
		// 4) array of references to stream objects
		// the last of these appears not to be supported by the PDF spec,
		// however we will accept it just in case
		PjObject contents = page.getContents();
		if (contents == null) {
			// set the page Contents to reference the new object
			page.setContents(objectToAdd);
		}
		else if (contents instanceof PjReference) {
			// find out whether the reference is to a stream or array
			PjObject indirectContents =
				getObject(((PjReference)contents).getObjNumber().getInt());
			if (indirectContents instanceof PjArray) {
				// add the new object to the existing array
				((PjArray)indirectContents).getVector().addElement(objectToAdd);
			}
			else if (indirectContents instanceof PjStream) {
				// create a new array that includes
				// the existing reference to the
				// stream as well as the new object
				// reference
				Vector v = new Vector();
				v.addElement(contents);
				v.addElement(objectToAdd);
				PjArray array = new PjArray(v);
				// add the new array to the document
				int arrayId = registerObject(array);
				// set the page Contents to reference this new array
				page.setContents(new PjReference(new PjNumber(arrayId)));
			}
			else {
				throw new InvalidPdfObjectException(
					"Contents reference in page does not reference a stream or array.");
			}
		}
		else if (contents instanceof PjArray) {
			// add the new object to the existing array
			((PjArray)contents).getVector().addElement(objectToAdd);
		}
		else {
			throw new InvalidPdfObjectException("Contents object in page is not a reference or array.");
		}
	}

	/**
	   Looks up a PjObject by its object number.
	   @param objectNumber the object number of the PjObject to retrieve.
	   @return the requested PjObject.
	 */
	public PjObject getObject(int objectNumber) {
		return _objects.objectAt(objectNumber);
	}

	/**
	   Dereferences a PjObject if it is a PjReference.
	   @param obj the PjObject to dereference.
	   @return the referenced PjObject if obj is a PjReference, or obj otherwise.
	 */
	public PjObject resolve(PjObject obj) {
		if (obj == null) {
			return null;
		} else {
			if (obj instanceof PjReference) {
				return resolve( getObject( ((PjReference)obj).getObjNumber().getInt() ) );
			} else {
				return obj;
			}
		}
	}

	/**
	   Determines the number of pages in this PDF document.
	   @return the number of pages in this PDF document.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	 */
	public int getPageCount() throws InvalidPdfObjectException {
		// the total number of pages should always be stored
		// in the root Pages node
		int pagesId = getRootPages();
		PjDictionary d;
		try {
			d = (PjDictionary)getObject(pagesId);
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Root pages object is not a dictionary.");
		}
		PjPages pages = new PjPages(d.getHashtable());

		PjObject countObj = pages.getCount();
		PjNumber count;
		try {
			count = (PjNumber)(resolve(countObj));
			if (count.isInteger() == false) {
				throw new ClassCastException();
			}
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Count field in root pages object is not an integer.");
		}
		return count.getInt();
	}

	private int findPage(int pageNumber, int objectNumber, PjPages parentPages, IntCounter counter, boolean delete)
		throws InvalidPdfObjectException {
		PjDictionary node;
		try {
			node = (PjDictionary)getObject(objectNumber);
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Object in page tree is not a dictionary.");
		}
		// figure out whether node is a Page or Pages object
		PjName type;
		try {
			type = (PjName)(node.getHashtable().get(PjName.TYPE));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException(
				"Type field in dictionary in page tree is not a name object.");
		}
		if (type.equals(PjName.PAGES)) {
			PjPages pages = new PjPages(node.getHashtable());
			PjArray kids;
			try {
				kids = (PjArray)(resolve((PjObject)(pages.getKids())));
			}
			catch (ClassCastException e) {
				throw new InvalidPdfObjectException("Kids field in pages object is not an array.");
			}
			if (kids != null) {
				Vector v = kids.getVector();
				int size = v.size();
				PjReference nodeRef;
				int found;
				for (int x = 0; x < size; x++) {
					try {
						nodeRef = (PjReference)(v.elementAt(x));
					}
					catch (ClassCastException e) {
						throw new InvalidPdfObjectException(
							"Object is kids array in pages object is not an indirect reference.");
					}
					found = findPage(pageNumber, nodeRef.getObjNumber().getInt(),
							 pages, counter, delete);
					if (found != -1) {
						if (delete) {
							// decrement the page count in this Pages node
							PjNumber count;
							try {
								count = (PjNumber)(resolve((PjObject)(pages.getCount())));
								if (count.isInteger() == false) {
									throw new ClassCastException();
								}
							}
							catch (ClassCastException e) {
								throw new InvalidPdfObjectException(
									"Count field in pages object is not an integer.");
							}
							pages.setCount(new PjNumber(count.getInt() - 1));
						}
						return found;
					}
				}
			}
			return -1;
		}
		if (type.equals(PjName.PAGE)) {
			counter.inc();
			if (counter.value() == pageNumber) {
				if (delete) {
					// remove the page from the kids array
					((PjArray)(parentPages.getKids())).getVector().removeElement(
						new PjReference(new PjNumber(objectNumber)));
				}
				return objectNumber;
			} else {
				return -1;
			}
		}
		return -1;
	}

	/**
	   Looks up a page in this document by page number.
	   @param pageNumber the page number.  Pages are numbered
	   starting with 1.
	   @return the object number of the identified Page object.
	   @exception IndexOutOfBoundsException if an invalid page
	   number was given.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/	   
	public int getPage(int pageNumber) throws IndexOutOfBoundsException, InvalidPdfObjectException {
		if (pageNumber < 1) {
			throw new IndexOutOfBoundsException("Page number " + pageNumber + " is not >= 1.");
		}
		IntCounter counter = new IntCounter(0);
		int found = findPage(pageNumber, getRootPages(), null, counter, false);
		if (found == -1) {
			if (pageNumber > getPageCount()) {
				throw new IndexOutOfBoundsException("Page number " + pageNumber + " is not <= " +
								    getPageCount() + ".");
			} else {
				throw new InvalidPdfObjectException("Page number " + pageNumber +
								   " not found; ran out of pages.");
			}
		} else {
			return found;
		}
	}

	/**
	   Deletes a page in this document by page number.  The page
	   is deleted by removing the reference to it from the page
	   tree; however, no objects are actually deleted from the
	   document.
	   @param pageNumber the page number.  Pages are numbered
	   starting with 1.
	   @return the object number of the deleted Page object.
	   @exception IndexOutOfBoundsException if an invalid page
	   number was given.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public int deletePage(int pageNumber) throws IndexOutOfBoundsException, InvalidPdfObjectException {
		if (pageNumber < 1) {
			throw new IndexOutOfBoundsException("Page number " + pageNumber + " is not >= 1.");
		}
		IntCounter counter = new IntCounter(0);
		int found = findPage(pageNumber, getRootPages(), null, counter, true);
		if (found == -1) {
			if (pageNumber > getPageCount()) {
				throw new IndexOutOfBoundsException("Page number " + pageNumber + " is not <= " +
								    getPageCount() + ".");
			} else {
				throw new InvalidPdfObjectException("Page number " + pageNumber +
								   " not found; ran out of pages.");
			}
		} else {
			return found;
		}
	}
	
	// we should split this out so that once we find the parent
	// node, we call a method to add the new page; we'll need to
	// use it in insertPage() also.
	/**
	   Appends a PjPage to the end of this PDF document.
	   @param objectNumber the object number of the PjPage to append.
	   @return the new object number of the appended PjPage.  */
	public int appendPage(int objectNumber) {
		// we do this the quickest way: go to the root Pages
		// node and add a link to the page at the top level.
		// this ignores the issue of maintaining a balanced
		// tree; we probably need some tree algorithms to deal
		// with general functions to manipulate the page tree.
		PjReference catalogRef = (PjReference)(_trailer.get(PjName.ROOT));
		PjDictionary catalog = (PjDictionary)getObject(catalogRef.getObjNumber().getInt());
		PjReference pagesRef = (PjReference)(catalog.getHashtable().get(PjName.PAGES));
		PjDictionary pages = (PjDictionary)getObject(pagesRef.getObjNumber().getInt());
		// we want to add the new page to the Kids array
		PjArray kids = (PjArray)(pages.getHashtable().get(PjName.KIDS));
		if (kids == null) {
			kids = new PjArray();
			pages.getHashtable().put(PjName.KIDS, kids);
		}
		kids.getVector().addElement( new PjReference(new PjNumber(objectNumber)) );
		// also need to set the parent
		PjPage page = (PjPage)getObject(objectNumber);
		page.setParent(pagesRef);
		// while we're here we need to increment the page count
		PjObject countObj = (PjObject)(pages.getHashtable().get(PjName.COUNT));
		PjNumber count = (PjNumber)resolve(countObj);
		int newCount = count.getInt() + 1;
		pages.getHashtable().put(PjName.COUNT, new PjNumber(newCount));
		return newCount;
	}

	/**
	   Appends the pages of a PDF document to this document.  Note
	   that this does not clone the other document but simply
	   includes references to its objects.  Therefore the other
	   document should be discarded immediately after a call to
	   this method, otherwise you could get very strange results.
	   @param pdf the PDF document to append.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered in either document.  */
	public void appendPdfDocument(Pdf pdf) throws InvalidPdfObjectException {

		// first gather some information

		// look up AcroForm in other document, and extract the
		// array of references to field objects; so that we
		// can add them to the field array in this document
		int otherCatalogId = pdf.getCatalog();
		PjCatalog otherCatalog;
		try {
			otherCatalog = (PjCatalog)(pdf.getObject(otherCatalogId));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Catalog object is not a dictionary.");
		}
		// get the AcroForm
		PjDictionary otherAcroForm;
		try {
			otherAcroForm = (PjDictionary)(pdf.resolve(otherCatalog.getAcroForm()));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("AcroForm object is not a dictionary.");
		}
		Vector otherFieldsV = null;
		if (otherAcroForm != null) {
			PjArray otherFields = (PjArray)(otherAcroForm.getHashtable().get(PjName.FIELDS));
			if (otherFields != null) {
				otherFieldsV = otherFields.getVector();
			}
		}
		
		// locate the root Pages node in the other document
		int pagesId = pdf.getRootPages();
		PjDictionary d;
		try {
			d = (PjDictionary)(pdf.getObject(pagesId));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Root pages object is not a dictionary.");
		}
		PjPages pages = new PjPages(d.getHashtable());

		// get the page count of the other document
		int pageCount = pdf.getPageCount();

		// locate the root Pages node in this document
		int thisPagesId = getRootPages();
		try {
			d = (PjDictionary)(getObject(thisPagesId));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Root pages object is not a dictionary.");
		}
		PjPages thisPages = new PjPages(d.getHashtable());

		// at this point we haven't changed anything

		// register all the objects with this document,
		// building a mapping table as we go along
		int id;
		PjObject obj;
		int pagesIdNew = -1;
		int size = pdf._objects.size();
		Hashtable map = new Hashtable(size);
		for (int x = 1; x < size; x++) {
			obj = pdf._objects.objectAt(x);
			if (obj != null) {
				id = registerObject(obj);
				// new object number for the root Pages node
				if (x == pagesId) {
					pagesIdNew = id;
				}
				// add mapping
				map.put(new PjNumber(x),
					new PjReference(new PjNumber(id)));
			}
		}

		// renumber objects
		// enumerate map as a way of enumerating the objects we added
		for (Enumeration m = map.keys(); m.hasMoreElements();) {
			// get the object number of an object we added
			id = ((PjReference)(map.get(m.nextElement()))).getObjNumber().getInt();
			obj = _objects.objectAt(id);
			if (obj instanceof PjReference) {
				registerObject((PjReference)(map.get(((PjReference)obj).getObjNumber())), id);
			} else {
				obj.renumber(map);
			}
		}
		
		// create a new root Pages node that includes the root nodes from the two documents
		PjPages newPages = new PjPages();
		int newPagesId = registerObject(newPages);
		Vector v = new Vector();
		v.addElement(new PjReference(new PjNumber(thisPagesId)));
		v.addElement(new PjReference(new PjNumber(pagesIdNew)));
		newPages.setKids(new PjArray(v));
		newPages.setCount(new PjNumber(getPageCount() + pageCount));
		// set the old root nodes' Parent to point to the new root node
		PjReference newPagesRef = new PjReference(new PjNumber(newPagesId));
		thisPages.setParent(newPagesRef);
		pages.setParent(newPagesRef);

		// update the catalog to point to the new root Pages node
		int catalogId = getCatalog();
		PjCatalog catalog;
		try {
			catalog = (PjCatalog)(getObject(catalogId));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Catalog object is not a dictionary.");
		}
		catalog.setPages(newPagesRef);

		// merge AcroForm from the two documents
		PjDictionary acroForm = (PjDictionary)(resolve(catalog.getAcroForm()));
		if (acroForm == null) {
			// use the other document's AcroForm
			PjDictionary otherAf = (PjDictionary)(otherCatalog.getAcroForm());
			if (otherAcroForm != null) {
				catalog.setAcroForm(otherAf);
			}
		} else {
			// add the fields extracted from other document's AcroForm
			// locate the fields array
			PjArray fields = (PjArray)(acroForm.getHashtable().get(PjName.FIELDS));
			if ( (otherFieldsV != null) && (fields != null) ) {
				Vector fieldsV = fields.getVector();
				int otherFieldsV_n = otherFieldsV.size();
				for (int x = 0; x < otherFieldsV_n; x++) {
					fieldsV.addElement(otherFieldsV.elementAt(x));
				}
			}
		}
		
	}

	/**
	   Looks up the Catalog object in this document.
	   @return the object number of the Catalog object.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	 */
	public int getCatalog() throws InvalidPdfObjectException {
		PjReference catalogRef;
		try {
			catalogRef = (PjReference)(_trailer.get(PjName.ROOT));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Root field in trailer is not an indirect reference.");
		}
		return catalogRef.getObjNumber().getInt();
	}
	
	/**
	   Looks up the root Pages object of this document's Pages tree.
	   @return the object number of the root Pages object.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	 */
	public int getRootPages() throws InvalidPdfObjectException {
		// we find the root Pages node via the Catalog object
		int catalogId = getCatalog();
		PjDictionary catalog;
		try {
			catalog = (PjDictionary)getObject(catalogId);
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Catalog is not a dictionary.");
		}
		PjReference pagesRef;
		try {
			pagesRef = (PjReference)(catalog.getHashtable().get(PjName.PAGES));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Pages field in catalog is not an indirect reference.");
		}
		return pagesRef.getObjNumber().getInt();
	}
	
	/**
	   Looks up the Info dictionary within this document's trailer.
	   The Info dictionary contains general information about the
	   document.
	   @return a reference to the Info dictionary, or null if no
	   Info field is present in the trailer.
	   @exception InvalidPdfObjectException if the Info field in
	   the trailer is not a reference (PjReference) object.
	*/
	public PjReference getInfoDictionary() throws InvalidPdfObjectException {
		PjReference r;
		try {
			r = (PjReference)(_trailer.get(PjName.INFO));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Info field is not an indirect reference.");
		}
		return r;
	}
	
	/**
	   Sets the Info dictionary within this document's trailer.
	   @param ref a reference to the Info dictionary.
	*/
	public void setInfoDictionary(PjReference ref) {
		_trailer.put(PjName.INFO, ref);
	}

	/**
	   Looks up the Encrypt dictionary within this document's trailer.
	   The Encrypt dictionary contains information for decrypting a
	   document.
	   @return the Encrypt dictionary, or null if no Encrypt field is
	   present in the trailer.
	   @exception InvalidPdfObjectException if the Encrypt field in
	   the trailer is not a dictionary (PjDictionary) object.
	*/
	public PjDictionary getEncryptDictionary() throws InvalidPdfObjectException {
		PjDictionary d;
		try {
			d = (PjDictionary)(resolve((PjObject)(_trailer.get(PjName.ENCRYPT))));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Encrypt field is not a dictionary.");
		}
		return d;
	}
	
	/**
	   Sets the Encrypt dictionary within this document's trailer.
	   @param ref a reference to the Encrypt dictionary.
	*/
	public void setEncryptDictionary(PjReference ref) {
		_trailer.put(PjName.ENCRYPT, ref);
	}

	/**
	   Sets the Encrypt dictionary within this document's trailer.
	   @param dict the Encrypt dictionary.
	*/
	public void setEncryptDictionary(PjDictionary dict) {
		_trailer.put(PjName.ENCRYPT, dict);
	}

	/**
	   Returns a clone of a pages node such that all inherited
	   attributes of the given pages node are made explicit.  For
	   example, if MediaBox is not defined in the given pages
	   node, this method ascends the pages tree (via the Parent
	   reference) looking for an ancestor node that does contain a
	   value for MediaBox; if it finds one, it assigns that value
	   in the cloned (returned) pages node.  This is done for all
	   inheritable attributes.
	   @param node a pages node for which inherited attributes are
	   to be retrieved.
	   @return a cloned copy of the given pages node with actual
	   values substituted for all inherited attributes.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public PjPagesNode inheritPageAttributes(PjPagesNode node) throws InvalidPdfObjectException {
		PjPagesNode newNode;
		try {
			newNode = (PjPagesNode)(node.clone());
		}
		catch (CloneNotSupportedException e) {
			throw new InvalidPdfObjectException(e.getMessage());
		}
		Hashtable ht = newNode.getHashtable();
		PjObject parentRef = newNode.getParent();
		while (parentRef != null) {
			PjObject parentObj = resolve(parentRef);
			if ( ! (parentObj instanceof PjPagesNode) ) {
				throw new InvalidPdfObjectException("Ancestor of pages node is not a pages node.");
			}
			PjPagesNode parent = (PjPagesNode)parentObj;
			inheritPageAttributesCollapse(PjName.MEDIABOX, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.RESOURCES, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.CROPBOX, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.ROTATE, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.DUR, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.HID, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.TRANS, ht, newNode, parent);
			inheritPageAttributesCollapse(PjName.AA, ht, newNode, parent);
			parentRef = parent.getParent();
		}
		return newNode;
	}

	/**
	   Returns a clone of a field node such that all inherited
	   attributes of the given field node are made explicit.  For
	   example, if the V key is not defined in the given field
	   node, this method ascends the field tree (via the Parent
	   reference) looking for an ancestor node that does contain a
	   value for the V key; if it finds one, it assigns that value
	   in the cloned (returned) field node.  This is done for all
	   inheritable attributes.
	   @param node a field node for which inherited attributes are
	   to be retrieved.
	   @return a cloned copy of the given field node with actual
	   values substituted for all inherited attributes.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public PjDictionary inheritFieldAttributes(PjDictionary node) throws InvalidPdfObjectException {
		PjDictionary newNode;
		try {
			newNode = (PjDictionary)(node.clone());
		}
		catch (CloneNotSupportedException e) {
			throw new InvalidPdfObjectException(e.getMessage());
		}
		Hashtable ht = newNode.getHashtable();
		PjObject parentRef = (PjObject)(newNode.getHashtable().get(PjName.PARENT));
		while (parentRef != null) {
			PjObject parentObj = resolve(parentRef);
			if ( ! (parentObj instanceof PjDictionary) ) {
				throw new InvalidPdfObjectException("Ancestor of field node is not a dictionary.");
			}
			PjDictionary parent = (PjDictionary)parentObj;
			inheritFieldAttributesCollapse(PjName.FT, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.V, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.DV, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.FF, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.DR, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.DA, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.Q, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.OPT, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.TOPINDEX, ht, newNode, parent);
			inheritFieldAttributesCollapse(PjName.MAXLEN, ht, newNode, parent);
			parentRef = (PjObject)(parent.getHashtable().get(PjName.PARENT));
		}
		return newNode;
	}

	/**
	   Returns the largest object number in the list of registered
	   PjObjects.  This is useful mainly for functions that need
	   to run through the list and process each object, because
	   this provides the maximum object number they need to
	   examine.  The object number may not currently be assigned
	   to an object, but probably was at some point in the past.
	   @return the size of the object list.
	*/
	public int getMaxObjectNumber() {
		return Math.max(_objects.size() - 1, 0);
	}


	public Vector getFields() throws InvalidPdfObjectException {

		Vector fieldList = new Vector();

		// get the Catalog
		int catalogId = getCatalog();
		PjCatalog catalog;
		try {
			catalog = (PjCatalog)(getObject(catalogId));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Catalog object is not a dictionary.");
		}

		// get the AcroForm
		PjDictionary acroForm;
		try {
			acroForm = (PjDictionary)(resolve(catalog.getAcroForm()));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("AcroForm object is not a dictionary.");
		}

		if (acroForm == null) {
			return fieldList;
		}
		
		// for now we assume that all root fields have no
		// children; so we treat Fields as an array

		// get Fields array
		PjArray fields = (PjArray)(acroForm.getHashtable().get(PjName.FIELDS));
		if (fields == null) {
			return fieldList;
		}
		Vector fieldsV = fields.getVector();
		
		// loop through all fields
		int fieldsV_n = fieldsV.size();
		for (int x = 0; x < fieldsV_n; x++) {

			// get the field object
			PjReference fieldRef;
			try {
				fieldRef = (PjReference)(fieldsV.elementAt(x));
			}
			catch (ClassCastException e) {
				throw new InvalidPdfObjectException("Fields array element is not a reference.");
			}

			getFieldsAddField(fieldList, fieldRef);

		}

		return fieldList;
		
	}


	private void getFieldsAddField(Vector fieldList, PjReference fieldRef)
		throws InvalidPdfObjectException {

		// resolve field reference
		PjDictionary field;
		try {
			field = (PjDictionary)(resolve(fieldRef));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Field object is not a dictionary.");
		}

		Hashtable fieldHt = field.getHashtable();

		// add the field to the list
		fieldList.addElement(field);
		
		// check if there are any kids
		PjArray kids;
		try {
			kids = (PjArray)(resolve((PjObject)(fieldHt.get(PjName.KIDS))));
		}
		catch (ClassCastException e) {
			throw new InvalidPdfObjectException("Kids object is not an array.");
		}
		
		// if there are kids, descend the tree
		if (kids != null) {
			Vector kidsV = kids.getVector();
			int kidsV_n = kidsV.size();
			for (int x = 0; x < kidsV_n; x++) {

				// get the field object
				PjReference fieldRef2;
				try {
					fieldRef2 = (PjReference)(kidsV.elementAt(x));
				}
				catch (ClassCastException e) {
					throw new InvalidPdfObjectException("Kids array element is not a reference.");
				}
				
				getFieldsAddField(fieldList, fieldRef2);

			}
		}
		
	}

	
	public void updateFieldValue(PjDictionary origField, PjDictionary field, String value)
		throws PdfFormatException, InvalidPdfObjectException {

		Hashtable origFieldHt = origField.getHashtable();

		Hashtable fieldHt = field.getHashtable();

		// store old value for use in search/replace within appeareances stream(s)
		PjString oldValue = (PjString)(fieldHt.get(PjName.V));
			
		PjString valueString = new PjString(value);
		origFieldHt.put(PjName.V, valueString);
		origFieldHt.put(PjName.DV, valueString);
		
		// determine quadding
		PjNumber q = (PjNumber)(resolve((PjObject)(fieldHt.get(PjName.Q))));
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

		PjDictionary ap = (PjDictionary)(resolve((PjObject)(fieldHt.get(PjName.AP))));
		if (ap != null) {
			Hashtable apHt = ap.getHashtable();
			PjObject apnObj = (PjObject)(apHt.get(PjName.N));
			int apnId;
			PjReference apnRef;
			PjObject apn;
			PjDictionary apnDict;
			byte[] apnBuffer;
			if (apnObj instanceof PjReference) {
				// it's an indirect object
				apnRef = (PjReference)apnObj;
				apnId = apnRef.getObjNumber().getInt();
				apn = resolve(apnRef);
			} else {
				// if it's not an indirect object, let's make it indirect
				apnId = registerObject(apnObj);
				apnRef = new PjReference(new PjNumber(apnId));
				apHt.put(PjName.N, apnRef);
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
				PjRectangle rect =
					(PjRectangle)(fieldHt.get(PjName.RECT));
				rectX1 = rect.getLowerLeftX().getFloat();
				rectX2 = rect.getUpperRightX().getFloat();
				rectWidth = rectX2 - rectX1;
			}

			if ( (apn != null) && (apn instanceof PjStream) ) {
				// if centered: remove any text matrix adjustments.
				// get page mark operators
				Vector pmVector = new StreamParser().parse(
					((PjStream)(apn)).flateDecompress());
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
				// register new (modified) stream in pdf document
				registerObject(new PjStream(((PjStream)(apn)).getStreamDictionary(), ba), apnId);
				
			}
		}
			    
	}
	

	// used exclusively by updateFieldValue()
	private static void replaceTextData(Vector pmVector, PjString oldText, PjString newText) {
		// this method replaces text data oldS with newS

		int pmX = pmVector.size();

		// no particular reason for searching backwards; just
		// because this was adapted from clearTextMatrixX()
		while (pmX > 0) {
			
			pmX--;
			PageMark pm = (PageMark)(pmVector.elementAt(pmX));
			
			if (pm instanceof XTj) {
				XTj tj = (XTj)pm;
				if (tj.getText().equals(oldText)) {
					XTj newTj = new XTj(newText);
					pmVector.setElementAt(newTj, pmX);
				}
			}
			
		}
	}

	
	// used exclusively by updateFieldValue()
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

	
	private void inheritPageAttributesCollapse(PjName name, Hashtable ht, PjPagesNode newNode, PjPagesNode parent) {
		if (ht.get(name) == null) {
			Object obj = parent.getHashtable().get(name);
			if (obj != null) {
				ht.put(name, obj);
			}
		}
	}


	private void inheritFieldAttributesCollapse(PjName name, Hashtable ht, PjDictionary newNode, PjDictionary parent) {
		if (ht.get(name) == null) {
			Object obj = parent.getHashtable().get(name);
			if (obj != null) {
				ht.put(name, obj);
			}
		}
	}


	private void init() {
		_objects = new PjObjectVector();
		_trailer = new Hashtable();
	}

	// this creates the minimal data structures for an empty Pdf object
	// (a single blank page)
	private void createEmpty() {
		// make a ProcSet
		Vector v = new Vector();
		v.addElement(PjName.PDF);
		v.addElement(PjName.TEXT);
		PjProcSet procSet = new PjProcSet(v);
		int procSetId = registerObject(procSet);
		// make a Resources dictionary
		PjResources resources = new PjResources();
		resources.setProcSet(new PjReference(new PjNumber(procSetId)));
		int resourcesId = registerObject(resources);
		// make a MediaBox rectangle
		PjRectangle mediaBox = new PjRectangle();
		mediaBox.setLowerLeftX(PjNumber.ZERO);
		mediaBox.setLowerLeftY(PjNumber.ZERO);
		mediaBox.setUpperRightX(new PjNumber(612));
		mediaBox.setUpperRightY(new PjNumber(792));
		// make a blank Page
		PjPage page = new PjPage();
		int pageId = registerObject(page);
		// make the kids array
		v = new Vector();
		v.addElement(new PjReference(new PjNumber(pageId)));
		PjArray kids = new PjArray(v);
		// make the root Pages node
		PjPages root = new PjPages();
		root.setResources(new PjReference(new PjNumber(resourcesId)));
		root.setMediaBox(mediaBox);
		root.setCount(PjNumber.ONE);
		root.setKids(kids);
		int rootId = registerObject(root);
		// we have to go back and set the blank page's parent to root
		page.setParent(new PjReference(new PjNumber(rootId)));
		// make the Catalog
		PjCatalog catalog = new PjCatalog();
		catalog.setPages(new PjReference(new PjNumber(rootId)));
		int catalogId = registerObject(catalog);
		// set Root in the trailer to point to the Catalog
		_trailer.put(PjName.ROOT, new PjReference(new PjNumber(catalogId)));
		// create an Info dictionary with default fields
		PjInfo info = new PjInfo();
		info.setCreator(PjConst.COPYRIGHT_IN_INFO);
		// need to add CreationDate and ModDate here, once we implement PjDate(Date)
		int infoId = registerObject(info);
		_trailer.put(PjName.INFO, new PjReference(new PjNumber(infoId)));
	}

	private void readFromFile(String filename) throws IOException,
		PjException {
		init();
		RandomAccessFile raf = new RandomAccessFile(filename,
							    "r");
		try {
			PdfParser.getObjects(this, raf);
		}
		finally {
			// make an attempt to close the file
			try {
				raf.close();
			}
			catch (IOException e) {
			}
		}
	}

	protected PjObjectVector _objects;
	protected Hashtable _trailer;

}
