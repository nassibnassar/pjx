package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Provides methods for retrieving and modifying the field tree of a
   PDF document.  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfFieldTree {

	/**
	   A stack for holding nested levels of field nodes.
	*/
	protected Stack _nested;

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

	/**
	   Defines the set of field attributes that are inheritable
	   from the AcroForm.
	*/
	protected static Set _inheritableAcroForm;

	protected static final PdfName PDFNAME_ACROFORM = new PdfName("AcroForm");
	protected static final PdfName PDFNAME_KIDS = new PdfName("Kids");
	protected static final PdfName PDFNAME_FIELDS = new PdfName("Fields");
	protected static final PdfName PDFNAME_PARENT = new PdfName("Parent");
	protected static final PdfName PDFNAME_T = new PdfName("T");
	protected static final PdfName PDFNAME_TYPE = new PdfName("Type");

	/**
	   Constructs a <code>PdfFieldTree</code> instance based on a
	   specified <code>PdfManager</code>.
	 */
	public PdfFieldTree(PdfManager manager) {

		_m = manager;
		_catalog = new PdfCatalog(manager);

		_inheritable = new HashSet(13);
		_inheritable.add(new PdfName("FT"));
		_inheritable.add(new PdfName("Ff"));
		_inheritable.add(new PdfName("V"));
		_inheritable.add(new PdfName("DV"));
		_inheritable.add(new PdfName("DR"));
		_inheritable.add(new PdfName("DA"));
		_inheritable.add(new PdfName("Q"));
		_inheritable.add(new PdfName("Opt"));
		_inheritable.add(new PdfName("MaxLen"));
		_inheritable.add(new PdfName("TI"));
		_inheritable.add(new PdfName("I"));
		_inheritable.add(new PdfName("Filter"));
		_inheritable.add(new PdfName("Flags"));
		
		_inheritableAcroForm = new HashSet(3);
		_inheritableAcroForm.add(new PdfName("DR"));
		_inheritableAcroForm.add(new PdfName("DA"));
		_inheritableAcroForm.add(new PdfName("Q"));

	}

	// the following should be made public once the API is stable
	/**
	   Returns the interactive form dictionary of the document.
	   This method returns a <code>PdfDictionary</code> (the
	   AcroForm dictionary), a <code>PdfReference</code> (an
	   indirect reference to the AcroForm dictionary), or
	   <code>null</code> if there is no AcroForm dictionary
	   present.
	   @return the AcroForm dictionary (direct object or indirect
	   reference) or <code>null</code>.
	   @throws IOException
	   @throws PdfFormatException
	 */
	protected PdfObject getAcroForm() throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				Object obj = _m.getObjectIndirect(_catalog.getCatalog());
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Catalog is not a dictionary.");
				}
				PdfDictionary catalog = (PdfDictionary)obj;

				obj = catalog.getMap().get(PDFNAME_ACROFORM);
				if (obj == null) {
					return null;
				}
				if ( ( !(obj instanceof PdfReference) ) &&
				     ( !(obj instanceof PdfDictionary) ) ) {
					throw new PdfFormatException(
						"AcroForm is not a dictionary or indirect reference.");
				}
				return (PdfObject)obj;

			}
		}
	}

	/**
	   Determines the fully qualified field name of a specified
	   field.
	   @param field the field dictionary.
	   @return the fully qualified field name.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public String getFullyQualifiedName(PdfDictionary field) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				// keep track of the approximate
				// length of the fully qualified field
				// name to use later when constructing
				// the StringBuffer
				int approxStringLength = 0;

				// the partial names will be stored in
				// a list
				ArrayList names = new ArrayList();

				// the first node we examine is the
				// specified dictionary
				PdfDictionary fieldNode = field;
				
				boolean done = false;
				do {

					Map fieldMap = fieldNode.getMap();
					
					// get the node's partial field name
					Object obj = fieldMap.get(PDFNAME_T);
					if (PdfNull.isNull(obj) == false) {
						if ( !(obj instanceof PdfObject) ) {
							throw new PdfFormatException(
								"Field name (T) is not a PDF object.");
						}
						obj = _m.getObjectIndirect((PdfObject)obj);
						if (PdfNull.isNull(obj) == false) {
							if ( !(obj instanceof PdfString) ) {
								throw new PdfFormatException(
									"Field name (T) is not a string.");
							}
							// add name to
							// running list
							String fieldName = ((PdfString)obj).getString();
							if (fieldName.length() > 0) {
								names.add(fieldName);
								approxStringLength += fieldName.length() + 1;
							}
						}
					}

					// ascend to the parent node
					obj = fieldMap.get(PDFNAME_PARENT);
					if (PdfNull.isNull(obj) == true) {
						done = true;
					} else {
						if ( !(obj instanceof PdfObject) ) {
							throw new PdfFormatException(
								"Field parent is not a PDF object.");
						}
						obj = _m.getObjectIndirect((PdfObject)obj);
						if (PdfNull.isNull(obj) == true) {
							done = true;
						} else {
							if ( !(obj instanceof PdfDictionary) ) {
								throw new PdfFormatException(
									"Field parent is not a dictionary.");
							}
							fieldNode = (PdfDictionary)obj;
						}
					}
					
				} while (!done);

				// now string the partial names together
				StringBuffer sb = new StringBuffer(approxStringLength);
				boolean first = true;
				int namesSize = names.size();
				for (int x = namesSize - 1; x >= 0; x--) {
					String name = (String)names.get(x);
					// append the name
					if (first) {
						first = false;
					} else {
						sb.append('.');
					}
					sb.append(name);
				}

				return sb.toString();
				
			}
		}
	}
	
	/**
	   Adds inherited attributes to a specified field dictionary
	   object.  The field object is cloned and the inherited
	   attributes are made explicit in the cloned object's
	   dictionary.  The inherited attributes are retrieved by
	   ascending the field tree and looking for inheritable
	   attributes (if any) that are missing from the specified
	   field dictionary.  The interactive form dictionary is also
	   checked (if necessary) for document-wide default values.
	   @param field the field dictionary to be filled in with
	   inherited attributes.
	   @return a clone of the specified field dictionary, with all
	   inherited attributes filled in.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfDictionary inheritAttributes(PdfDictionary field) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				Map fieldM = field.getMap();
				
				// define new dictionary map
				Map newMap = new HashMap(field.getMap());
		
				// start out looking for all inheritable attributes
				// that are not present in this field
				Set unused = new HashSet(_inheritable.size());
				for (Iterator t = _inheritable.iterator(); t.hasNext(); ) {
					
					PdfName attr = (PdfName)t.next();
					Object obj = fieldM.get(attr);
					
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
					Object obj = fieldM.get(PDFNAME_PARENT);
					if (obj == null) {
						// we are done, but we
						// need to do one more
						// round of
						// inheritance from
						// the AcroForm
						done = true;
						obj = getAcroForm();
						if (obj == null) {
							break;
						}
						// remove all elements
						// from the unused set
						// except for
						// AcroForm-inheritable fields
						unused.retainAll(_inheritableAcroForm);
						if (unused.isEmpty()) {
							break;
						}
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
					fieldM = ((PdfDictionary)obj).getMap();
					
					// now examine the parent node
					for (Iterator t = unused.iterator(); t.hasNext(); ) {
						
						PdfName attr = (PdfName)t.next();
						
						// check if the attribute is present
						obj = fieldM.get(attr);
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
	
	/**
	   Returns an iterator over the terminal field objects in this
	   document's field tree.  Note that terminal field objects do
	   not include inherited attributes; {@link
	   #inheritAttributes(PdfDictionary)
	   inheritAttributes(PdfDictionary)} should be used to obtain
	   inherited attributes.
	   @return the iterator over the terminal field objects.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfFieldTreeIterator getIterator() throws IOException, PdfFormatException {
		return new FieldTreeIterator(this, _m);
	}

	/**
	   An iterator over the tree of field dictionaries in a PDF document.
	   @author Nassib Nassar
	 */
	protected class FieldTreeIterator implements PdfFieldTreeIterator {

		/**
		   The manager associated with this iterator.
		*/
		protected PdfManager _m;

		/**
		   The field tree associated with this iterator.
		*/
		protected PdfFieldTree _ft;

		/**
		   Constructs an iterator over a field tree.
		   @param ft the field tree to iterate over.
		   @param m the associated document manager.
		   @throws IOException
		   @throws PdfFormatException
		 */
		public FieldTreeIterator(PdfFieldTree ft, PdfManager m) throws IOException, PdfFormatException {

			_ft = ft;
			_m = m;
			_nested = new Stack();

			// get the AcroForm
			Object obj = m.getObjectIndirect(getAcroForm());

			if (obj != null) {
				
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"AcroForm is not a dictionary.");
				}
				PdfDictionary acroForm = (PdfDictionary)obj;
				
				// get the Fields array from the AcroForm
				obj = acroForm.getMap().get(PDFNAME_FIELDS);
				if ( !(obj instanceof PdfObject) ) {
					throw new PdfFormatException(
						"Fields array is not a PDF object.");
				}
				obj = m.getObjectIndirect((PdfObject)obj);
				if ( !(obj instanceof PdfArray) ) {
					throw new PdfFormatException(
						"Fields array is not an array.");
				}			
				List fields = ((PdfArray)obj).getList();
				
				_nested.push(newList(fields));

			}
			
		}

		protected List newList(List list) {
			return new ArrayList(list);
		}

		/**
		   Descends the left-edge of the tree until reaching a
		   terminal node and returns its reference.  As the
		   tree is descended, this method pushes field lists
		   onto the stack.
		   @return an indirect reference to the terminal node
		   @throws IOException
		   @throws PdfFormatException
		 */
		protected PdfReference descendTree() throws IOException, PdfFormatException {

			List kidsM;
			PdfReference node;
			
			do {

				// get the first element of the field
				// list in the top element of the
				// stack; this assumes there is a
				// field list on the stack, and that
				// the field list contains at least
				// one element
				List fieldsM = (List)_nested.peek();
				node = (PdfReference)fieldsM.get(0);

				// remove the element
				fieldsM.remove(0);

				// retrieve the referenced node
				Object obj = _m.getObjectIndirect(node);
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Field node is not a dictionary.");
				}
				Map fieldNode = ((PdfDictionary)obj).getMap();

				// determine if there are any children

				obj = fieldNode.get(PDFNAME_KIDS);

				if (obj == null) {

					kidsM = null;

				} else {

					if ( !(obj instanceof PdfObject) ) {
						throw new PdfFormatException(
							"Kids array is not a PDF object.");
					}
					obj = _m.getObjectIndirect((PdfObject)obj);
					if ( !(obj instanceof PdfArray) ) {
						throw new PdfFormatException(
							"Kids array is not an array object.");
					}
					List kidsL = ((PdfArray)obj).getList();
					if (kidsL.isEmpty()) {
						throw new PdfFormatException(
							"Kids array is empty.");
					}

					kidsM = newList(kidsL);

				}
				// now kidsM contains a modifiable
				// version of the kids array, or it
				// equals null if there is no kids
				// array

				// push the kids onto the stack and
				// continue to the next level of
				// descent
				if (kidsM != null) {
					_nested.push(kidsM);
				}

			} while (kidsM != null);
			// at this point, kidsM == null, which means
			// that fieldNode is a terminal node

			return node;
			
		}

		/**
		   Removes any empty lists from the top of the stack.
		 */
		protected void cleanUp() {

			while ( ( _nested.empty() == false ) &&
				( ((List)_nested.peek()).isEmpty() ) ) {

				_nested.pop();
				
			}

		}
		
		// Clean-up, Check Null Stack
		public boolean hasNext() throws PdfFormatException {
			synchronized (_m) {
				synchronized (_ft) {

					cleanUp();
					
					return !(_nested.empty());
					
				}
			}
		}

		/**
		   @throws IOException
		   @throws PdfFormatException
		*/
		public PdfReference next() throws NoSuchElementException, IOException, PdfFormatException {
			synchronized (_m) {
				synchronized (_ft) {

					// throw an exception if there
					// are no more elements
					if ( !hasNext() ) {
						throw new NoSuchElementException();
					}

					// descend the tree to a
					// terminal node
					return descendTree();
					
				}
			}
		}

	}
	
}
