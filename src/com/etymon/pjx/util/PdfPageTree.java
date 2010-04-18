package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Provides methods for retrieving and modifying the page tree of a
   PDF document.  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfPageTree {

	/**
	   The page tree root of the document.
	*/
	protected PdfReference _pageTreeRoot;

	/**
	   The manager associated with this document.
	*/
	protected PdfManager _m;

	/**
	   The catalog associated with this document.
	*/
	protected PdfCatalog _catalog;

	/**
	   Defines the set of inheritable field attributes.
	*/
	protected static Set _inheritable;

	protected static final PdfName PDFNAME_COUNT = new PdfName("Count");
	protected static final PdfName PDFNAME_KIDS = new PdfName("Kids");
	protected static final PdfName PDFNAME_PAGE = new PdfName("Page");
	protected static final PdfName PDFNAME_PAGES = new PdfName("Pages");
	protected static final PdfName PDFNAME_PARENT = new PdfName("Parent");
	protected static final PdfName PDFNAME_TYPE = new PdfName("Type");

	/**
	   Constructs a <code>PdfPageTree</code> instance based on a
	   specified <code>PdfManager</code>.
	 */
	public PdfPageTree(PdfManager manager) {

		_m = manager;
		_catalog = new PdfCatalog(manager);

		_inheritable = new HashSet(4);
		_inheritable.add(new PdfName("Resources"));
		_inheritable.add(new PdfName("MediaBox"));
		_inheritable.add(new PdfName("CropBox"));
		_inheritable.add(new PdfName("Rotate"));
		
	}

	/**
	   Returns an indirect reference to a page object specified by
	   page number.  Note that page objects do not include
	   inherited attributes; {@link
	   #inheritAttributes(PdfDictionary)
	   inheritAttributes(PdfDictionary)} should be used to obtain
	   inherited attributes.
	   @param pageNumber the page number.  The numbering starts
	   with <code>0</code>.
	   @return the indirect reference.
	   @throws IOException
	   @throws PdfFormatException
	 */
        public PdfReference getPage(int pageNumber) throws IOException, PdfFormatException {
                synchronized (this) {
			synchronized (_m) {

				if (pageNumber < 0) {
					throw new IndexOutOfBoundsException(
						"Requested page number is less than 0");
				}

				// keep a running list of all page
				// nodes visited so that we can detect
				// a cycle and avoid getting caught in
				// an infinite loop
				Set visited = new HashSet();

				// get the root of the page tree
				PdfReference nodeR = getRoot();
				visited.add(nodeR);
				Object obj = _m.getObjectIndirect(nodeR);
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Page tree (Pages) is not a dictionary.");
				}
				Map node = ((PdfDictionary)obj).getMap();

				// descend the page tree; each
				// iteration through this loop
				// descends one level
				boolean first = true;
				int numberOfPages;
				int pageSum = 0;
				while ( true ) {

					// if this is the first node,
					// it should contain the
					// total number of pages;
					// check that the requested
					// page is within that range
					if (first) {

						first = false;
						
						obj = node.get(PDFNAME_COUNT);
						if ( ( !(obj instanceof PdfInteger) ) &&
						     ( !(obj instanceof PdfReference) ) ) {
							throw new PdfFormatException(
								"Page count is not an integer or reference.");
						}
						if (obj instanceof PdfReference) {
							obj = _m.getObjectIndirect((PdfReference)obj);
						}
						if ( !(obj instanceof PdfInteger) ) {
							throw new PdfFormatException(
								"Page count is not an integer.");
						}
						numberOfPages = ((PdfInteger)obj).getInt();

						if (pageNumber >= numberOfPages) {
							throw new IndexOutOfBoundsException(
								"Requested page number is too large");
						}
						
					}

					// at this point we have a
					// node that is not a page
					// object; therefore we assume
					// it is a pages object and
					// proceed to determine the
					// next node to examine
					
					// get the list of kids
					obj = node.get(PDFNAME_KIDS);
					if ( ( !(obj instanceof PdfArray) ) &&
					     ( !(obj instanceof PdfReference) ) ) {
						throw new PdfFormatException(
							"Kids object is not an array or reference.");
					}
					if (obj instanceof PdfReference) {
						obj = _m.getObjectIndirect((PdfReference)obj);
					}
					if ( !(obj instanceof PdfArray) ) {
						throw new PdfFormatException(
							"Kids object is not an array.");
					}
					List kids = ((PdfArray)obj).getList();
//System.out.println( (PdfArray)obj );
					
					// iterate through the list of
					// kids, examining the number
					// of pages in each, and
					// stopping when we reach the
					// one that must contain the
					// page we are looking for
					boolean descend = false;
					for (Iterator t = kids.iterator(); ( (t.hasNext()) && (!descend) ); ) {

						// get the "kid",
						// i.e. the referenced
						// page or pages
						// object
						obj = t.next();
						if ( !(obj instanceof PdfReference) ) {
							throw new PdfFormatException(
								"Kids element is not a reference.");
						}
						PdfReference kidR = ((PdfReference)obj);
						if (visited.contains(kidR)) {
							throw new PdfFormatException(
								"Page tree contains a cycle (must be acyclic).");
						}
						visited.add(kidR);
						obj = _m.getObjectIndirect(kidR);
						if ( !(obj instanceof PdfDictionary) ) {
							throw new PdfFormatException(
								"Kids element is not a dictionary.");
						}
						Map kid = ((PdfDictionary)obj).getMap();

						// determine whether
						// it is a page object
						// or a pages object
						obj = kid.get(PDFNAME_TYPE);
						if ( ( !(obj instanceof PdfName) ) &&
						     ( !(obj instanceof PdfReference) ) ) {
							throw new PdfFormatException(
								"Page node type is not a name or reference.");
						}
						if (obj instanceof PdfReference) {
							obj = _m.getObjectIndirect((PdfReference)obj);
						}
						if ( !(obj instanceof PdfName) ) {
							throw new PdfFormatException(
								"Page node type is not a name.");
						}
						PdfName nodeType = (PdfName)obj;
						boolean singlePage = nodeType.equals(PDFNAME_PAGE);
						
						// determine how many
						// pages are
						// represented by this
						// node
						int count;
						if (singlePage) {
							// this is a
							// page
							// object, so
							// it
							// represents
							// exactly one
							// page
							count = 1;
						} else {
							// otherwise
							// we assume
							// this is a
							// pages
							// object, and
							// we examine
							// the Count
							// value
							obj = kid.get(PDFNAME_COUNT);
							if ( ( !(obj instanceof PdfInteger) ) &&
							     ( !(obj instanceof PdfReference) ) ) {
								throw new PdfFormatException(
									"Page count is not an integer or reference.");
							}
							if (obj instanceof PdfReference) {
								obj = _m.getObjectIndirect((PdfReference)obj);
							}
							if ( !(obj instanceof PdfInteger) ) {
								throw new PdfFormatException(
									"Page count is not an integer.");
							}
							count = ((PdfInteger)obj).getInt();
						}

						if ( (pageSum + count) > pageNumber ) {

							if (singlePage) {
								// this is the page we are looking for
								return kidR;
							} else {
								// descend this node; don't bother with the rest of the
								// kids in the list
								node = kid;
								descend = true;
							}
							
						} else {

							// we will
							// keep
							// iterating
							// the kid
							// list, so we
							// add the
							// number of
							// pages to
							// the left to
							// our running
							// sum
							pageSum += count;

						}
						
					} // for()

					// if descend was not set to
					// true, then the for() loop
					// completed normally, meaning
					// that the kids do not
					// contains enough pages, and
					// something is wrong with the
					// document
					if ( !descend ) {
						throw new PdfFormatException(
							"Requested page not found.");
					}
					
				} // while()
			}
		}
	}
	
        /**
           Returns the number of pages in the document.
           @return the number of pages.
           @throws IOException
           @throws PdfFormatException
         */
        public int getNumberOfPages() throws IOException, PdfFormatException {
                synchronized (this) {
			synchronized (_m) {

				Object obj = _m.getObjectIndirect(getRoot());

				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Page tree root (Pages) is not a dictionary.");
				}

				Map root = ((PdfDictionary)obj).getMap();

				obj = root.get(PDFNAME_COUNT);
				
				if ( ( !(obj instanceof PdfInteger) ) &&
				     ( !(obj instanceof PdfReference) ) ) {
					throw new PdfFormatException(
						"Page count is not an integer or reference.");
				}
				
				if (obj instanceof PdfReference) {
					obj = _m.getObjectIndirect((PdfReference)obj);
				}
						
				if ( !(obj instanceof PdfInteger) ) {
					throw new PdfFormatException(
						"Page count is not an integer.");
				}

				return ((PdfInteger)obj).getInt();
				
			}                        
                }
        }

	/**
	   Returns an indirect reference to the root node of the
	   document's page tree.
	   @return the indirect reference.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfReference getRoot() throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				Object obj = _m.getObjectIndirect(_catalog.getCatalog());
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Catalog is not a dictionary.");
				}
				PdfDictionary catalog = (PdfDictionary)obj;

				obj = catalog.getMap().get(PDFNAME_PAGES);
				if ( !(obj instanceof PdfReference) ) {
					throw new PdfFormatException(
						"Page tree root (Pages) is not an indirect reference.");
				}
				return (PdfReference)obj;

			}
		}
	}

	/**
	   Adds inherited attributes to a specified page dictionary
	   object.  The page object is cloned and the inherited
	   attributes are made explicit in the cloned object's
	   dictionary.  The inherited attributes are retrieved by
	   ascending the page tree and looking for inheritable
	   attributes (if any) that are missing from the specified
	   page dictionary.
	   @param page the page dictionary to be filled in with
	   inherited attributes.
	   @return a clone of the specified page dictionary, with all
	   inherited attributes filled in.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfDictionary inheritAttributes(PdfDictionary page) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				Map pageM = page.getMap();
				
				// define new dictionary map
				Map newMap = new HashMap(page.getMap());
		
				// start out looking for all inheritable attributes
				// that are not present in this page
				Set unused = new HashSet(_inheritable.size());
				for (Iterator t = _inheritable.iterator(); t.hasNext(); ) {
					
					PdfName attr = (PdfName)t.next();
					Object obj = pageM.get(attr);
					
					if ( (obj == null) || (obj instanceof PdfNull) ) {
						unused.add(attr);
					}
					
				}
				
				boolean done = false;
				
				do {
					
					// if all the inheritable attributes have been
					// filled, there is no need to continue
					// ascending the tree
					if (unused.isEmpty()) {
						done = true;
						break;
					}
					
					// get the Parent node
					Object obj = pageM.get(PDFNAME_PARENT);
					if (obj == null) {
						// we are done
						done = true;
						break;
					}
					if ( !(obj instanceof PdfObject) ) {
						throw new PdfFormatException(
							"Parent object is not a PDF object.");
					}
					obj = _m.getObjectIndirect((PdfObject)obj);
					if ( !(obj instanceof PdfDictionary) ) {
						throw new PdfFormatException(
							"Parent object is not a dictionary.");
					}
					pageM = ((PdfDictionary)obj).getMap();
					
					// now examine the parent node
					for (Iterator t = unused.iterator(); t.hasNext(); ) {
						
						PdfName attr = (PdfName)t.next();
						
						// check if the attribute is present
						obj = pageM.get(attr);
						if ( (obj != null) && ( !(obj instanceof PdfNull) ) ) {
							t.remove();
							newMap.put(attr, obj);
						}
						
					}
					
				} while ( !done );

				return new PdfDictionary(newMap);

			}
		}
	}
	
}
