package com.etymon.pjx.util;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Examines a specified object and returns the set of all objects it
   references.  This class implements the {@link PdfObjectFilter
   PdfObjectFilter} interface.  The {@link #preFilter(PdfObject)
   preFilter(PdfObject)} method can be overridden in a subclass to
   pre-process the object and its contents.  This class is
   synchronized.
   @author Nassib Nassar
*/
public class PdfReferencedObjects implements PdfObjectFilter {

	/**
	   The total set of referenced objects (stored as indirect
	   references).
	 */
	protected Set _ref_master;

	/**
	   The current set of referenced objects (stored as indirect
	   references).
	 */
	protected Set _ref;

	/**
	   The manager to use for resolving references.
	 */
	protected PdfManager _m;
	
	/**
	   Constructs a <code>PdfReferencedObjects</code> instance.
	   @param manager the manager associated with the document.
	 */
	public PdfReferencedObjects(PdfManager manager) {

		_m = manager;
		
	}

	/**
	   Returns the set of all objects referenced by the specified
	   PDF object.  This method calls {@link
	   PdfObject#filter(PdfObjectFilter)
	   PdfObject.filter(PdfObjectFilter)} to process objects
	   recursively.
	   @param obj the object to examine.
	   @throws PdfFormatException
	 */
	public Set getReferenced(PdfObject obj) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				// get initial set of objects
				_ref_master = new HashSet();
				_ref = new HashSet();
				PdfObject newObj = _m.getObjectIndirect(obj);
				newObj.filter(this);

				_ref_master = _ref;
				_ref = new HashSet();
				
				// now loop until all references have
				// been resolved
				Set left = new HashSet(_ref_master);
				while (left.isEmpty() == false) {

					// take one element
					PdfReference t = (PdfReference)left.iterator().next();
					left.remove(t);

					newObj = _m.getObjectIndirect(t);
					newObj.filter(this);

					left.addAll(_ref);
					_ref_master.addAll(_ref);
					_ref.clear();
					
				}

				_ref = null;
				Set r = _ref_master;
				_ref_master = null;
				return r;

			}
		}
	}

	/**
	   This method is used by {@link #getReferenced(PdfObject)
	   getReferenced(PdfObject)} and <b>should not be called
	   externally</b>; however, it may be overridden in subclasses
	   in order to pre-process the objects.  (It is not
	   synchronized.)
	   @param obj the object to filter.
	   @return the filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject preFilter(PdfObject obj) throws PdfFormatException {
		return obj;
	}
	
	/**
	   This method is used by {@link #getReferenced(PdfObject)
	   getReferenced(PdfObject)} and <b>should not be called
	   externally</b>.  (It is not synchronized.)
	   @param obj the object to filter.
	   @return the filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject postFilter(PdfObject obj) throws PdfFormatException {
		if (obj instanceof PdfReference) {
			if (_ref_master.contains(obj) == false) {
				_ref.add(obj);
			}
		}
		return obj;
	}
	
}
