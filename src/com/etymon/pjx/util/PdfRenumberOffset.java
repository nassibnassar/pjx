package com.etymon.pjx.util;

import com.etymon.pjx.*;

/**
   Modifies indirect references within a PDF object, adding a
   specified offset to each object number in a reference.  {@link
   PdfObjectFilter PdfObjectFilter} is used to filter the indirect
   references.  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfRenumberOffset implements PdfObjectFilter {

	/**
	   The offset value to add to each object number.
	*/
	protected int _offset;

	/**
	   Controls whether generation numbers will be set to 0 during
	   the renumbering process.  If so, the value is
	   <code>true</code>.
	*/
	protected boolean _resetG;

	/**
	   Constructs a <code>PdfRenumberOffset</code> instance.  By
	   default, generation numbers will not be modified (see
	   {@link #resetGeneration(boolean)
	   resetGeneration(boolean)}).  The offset value defaults to 0
	   (see {@link #setOffset(int) setOffset(int)}).
	 */
	public PdfRenumberOffset() {

		_offset = 0;
		_resetG = false;
		
	}

	/**
	   Sets the offset value to add to each object number during
	   renumbering.
	   @param offset the offset to use.
	 */
	public void setOffset(int offset) {
		synchronized (this) {

			_offset = offset;
			
		}
	}

	/**
	   Controls whether generation numbers will be set to 0 during
	   the renumbering process.
	   @param reset if <code>true</code>, generation numbers will
	   be set to 0; otherwise they are not modified.
	 */
	public void resetGeneration(boolean reset) {
		synchronized (this) {

			_resetG = reset;
			
		}
	}

	/**
	   Adds an offset to the object number in each {@link
	   PdfReference PdfReference} within the specified object.
	   The generation number may optionally be reset to 0 (see
	   {@link #resetGeneration(boolean)
	   resetGeneration(boolean)}).  The offset is specified with
	   {@link #setOffset(int) setOffset(int)}.
	   @param obj the object to renumber.
	   @return the renumbered object.
	   @throws PdfFormatException
	 */
	public PdfObject renumber(PdfObject obj) throws PdfFormatException {
		synchronized (this) {

			return obj.filter(this);
			
		}
	}

	/**
	   This method is used by {@link #renumber(PdfObject)
	   renumber(PdfObject)} to filter indirect references and
	   <b>should not be called externally</b>.  (It is not
	   synchronized.)
	   @param obj the object to filter.
	   @return the filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject preFilter(PdfObject obj) throws PdfFormatException {
		if (obj instanceof PdfReference) {
 			PdfReference r = (PdfReference)obj;
 			return new PdfReference( r.getObjectNumber() + _offset,
 						 _resetG ? 0 : r.getGenerationNumber() );
		} else {
			return obj;
		}
	}
	
	/**
	   This method is used by {@link #renumber(PdfObject)
	   renumber(PdfObject)} to filter indirect references and
	   <b>should not be called externally</b>.  (It is not
	   synchronized.)
	   @param obj the object to filter.
	   @return the filtered object.
	   @throws PdfFormatException
	 */
	public PdfObject postFilter(PdfObject obj) throws PdfFormatException {
		return obj;
	}
	
}
