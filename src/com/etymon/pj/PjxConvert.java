/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pj;

import java.util.*;
import java.io.*;
import java.nio.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;
import com.etymon.pj.object.pagemark.*;
import com.etymon.pjx.*;
import com.etymon.pjx.util.*;

/**
   @author Nassib Nassar
   @deprecated
*/
public class PjxConvert {

	protected static final PdfName PDFNAME_LENGTH = new PdfName("Length");

	public static PjObject toPjObject(PdfObject obj) throws com.etymon.pj.exception.PdfFormatException {

		if (obj instanceof PdfNull) {
			return new PjNull();
		}

		if (obj instanceof PdfBoolean) {
			return new PjBoolean( ((PdfBoolean)obj).getBoolean() );
		}

		if (obj instanceof PdfInteger) {
			return new PjNumber( (float)((PdfInteger)obj).getInt() );
		}
		
		if (obj instanceof PdfFloat) {
			return new PjNumber( ((PdfFloat)obj).getFloat() );
		}
		
		if (obj instanceof PdfString) {
			return new PjString( ((PdfString)obj).getString() );
		}

		if (obj instanceof PdfName) {
			return new PjName( ((PdfName)obj).getString() );
		}

		if (obj instanceof PdfArray) {
			List list = ((PdfArray)obj).getList();
			Vector v = new Vector();
			for (Iterator it = list.iterator(); it.hasNext(); ) {
				v.addElement( toPjObject((PdfObject)it.next()) );
			}
			return new PjArray(v);
		}

		if (obj instanceof PdfDictionary) {
			Map map = ((PdfDictionary)obj).getMap();
			Hashtable h = new Hashtable();
			for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
				Object key = it.next();
				h.put(toPjObject((PdfObject)key), toPjObject((PdfObject)map.get(key)) );
			}

			// figure out what kind of dictionary we have
			PjDictionary dictionary = new PjDictionary(h);
			if (PjPage.isLike(dictionary)) {
				return new PjPage(h);
			}
			else if (PjPages.isLike(dictionary)) {
				return new PjPages(h);
			}
			else if (PjFontType1.isLike(dictionary)) {
				return new PjFontType1(h);
			}
			else if (PjFontDescriptor.isLike(dictionary)) {
				return new PjFontDescriptor(h);
			}
			else if (PjResources.isLike(dictionary)) {
				return new PjResources(h);
			}
			else if (PjCatalog.isLike(dictionary)) {
				return new PjCatalog(h);
			}
			else if (PjInfo.isLike(dictionary)) {
				return new PjInfo(h);
			}
			else if (PjEncoding.isLike(dictionary)) {
				return new PjEncoding(h);
			}
			else {
				return dictionary;
			}
		}

		if (obj instanceof PdfStream) {
			PdfStream stream = (PdfStream)obj;
			PjDictionary d = (PjDictionary)(toPjObject(stream.getDictionary()));
			ByteBuffer streambuffer = stream.getBuffer();
			byte[] ba = new byte[streambuffer.capacity()];
			streambuffer.position(0);
			streambuffer.limit(streambuffer.capacity());
			streambuffer.get(ba);
			return new PjStream(new PjStreamDictionary(d.getHashtable()), ba);
		}

		if (obj instanceof PdfReference) {
			PdfReference ref = (PdfReference)obj;
			return new PjReference(new PjNumber(ref.getObjectNumber()),
					       new PjNumber(ref.getGenerationNumber()) );
		}

		throw new com.etymon.pj.exception.PdfFormatException("Error converting object from PJX format.");
		
	}
	
	public static PdfObject toPjxObject(PjObject obj) throws com.etymon.pj.exception.PdfFormatException {

		if (obj instanceof PjNull) {
			return PdfNull.valueOf();
		}
		
		if (obj instanceof PjBoolean) {
			return PdfBoolean.valueOf( ((PjBoolean)obj).getBoolean() );
		}
		
		if (obj instanceof PjNumber) {
			PjNumber n = (PjNumber)obj;
			float f = n.getFloat();
			int x = n.getInt();
			if (f == x) {
				return new PdfInteger(x);
			} else {
				return new PdfFloat(f);
			}
		}
		
		if (obj instanceof PjString) {
			return new PdfString( ((PjString)obj).getString() );
		}
		
		if (obj instanceof PjName) {
			return new PdfName( ((PjName)obj).getString() );
		}
		
		if (obj instanceof PjArray) {
			Vector v = ((PjArray)obj).getVector();
			int vsize = v.size();
			ArrayList list = new ArrayList(vsize);
			for (int x = 0; x < vsize; x++) {
				list.add( toPjxObject((PjObject)v.elementAt(x)) );
			}
			return new PdfArray(list);
		}
		
		if (obj instanceof PjDictionary) {
			Hashtable h = ((PjDictionary)obj).getHashtable();
			HashMap map = new HashMap(h.size());
			for (Enumeration m = h.keys(); m.hasMoreElements();) {
				PjObject key = (PjObject)m.nextElement();
				PjObject value = (PjObject)h.get(key);
				map.put( toPjxObject(key), toPjxObject(value) );
			}
			return new PdfDictionary(map);
		}
		
		if (obj instanceof PjStream) {
			PjStream os = (PjStream)obj;
			byte[] ob = os.getBuffer();
			ByteBuffer buffer = ByteBuffer.allocateDirect(ob.length);
			buffer.put(ob);
			Map map = ((PdfDictionary)toPjxObject(os.getStreamDictionary())).getMap();
			HashMap dictionary = new HashMap(map.size());
			dictionary.putAll(map);
			dictionary.put(PDFNAME_LENGTH, new PdfInteger(ob.length));
			buffer.position(0);
			return new PdfStream(new PdfDictionary(dictionary), buffer);
		}
		
		if (obj instanceof PjReference) {
			PjReference r = (PjReference)obj;
			return new PdfReference( r.getObjNumber().getInt(),
						 r.getGenNumber().getInt() );
		}

		throw new com.etymon.pj.exception.PdfFormatException("Error converting object from PJ format.");
		
	}

	/**
	   This method is provided for compatibility with PJ.
	   @throws IOException
	   @throws PdfFormatException
	   @deprecated
	 */
	public static List pjxGetFields(PdfManager manager) throws IOException, com.etymon.pjx.PdfFormatException {

		synchronized (manager) {

			PdfModifier mod = new PdfModifier(manager);
			
			ArrayList fieldList = new ArrayList();
			
			// get the Catalog
			PdfDictionary catalog = mod.getCatalog();
			
			// get the AcroForm
			PdfDictionary acroForm;
			try {
				acroForm = (PdfDictionary)(manager.getObjectIndirect(
								   (PdfObject)catalog.getMap().get(new PdfName("AcroForm")) ));
			}
			catch (ClassCastException e) {
				throw new com.etymon.pjx.PdfFormatException("AcroForm object is not a dictionary.");
			}
			
			if (acroForm == null) {
				return fieldList;
			}
			
			// for now we assume that all root fields have no
			// children; so we treat Fields as an array
			
			// get Fields array
			PdfArray fields = (PdfArray)(acroForm.getMap().get(new PdfName("Fields")));
			if (fields == null) {
				return fieldList;
			}
			List fieldsV = fields.getList();
			
			// loop through all fields
			int fieldsV_n = fieldsV.size();
			for (int x = 0; x < fieldsV_n; x++) {
				
				// get the field object
				PdfReference fieldRef;
				try {
					fieldRef = (PdfReference)(fieldsV.get(x));
				}
				catch (ClassCastException e) {
					throw new com.etymon.pjx.PdfFormatException("Fields array element is not a reference.");
				}
				
				pjxGetFieldsAddField(manager, fieldList, fieldRef);
				
			}
			
			return fieldList;
		}
	}

	/**
	   @deprecated
	 */
	private static void pjxGetFieldsAddField(PdfManager manager, ArrayList fieldList, PdfReference fieldRef)
		throws IOException, com.etymon.pjx.PdfFormatException {

		// resolve field reference
		PdfDictionary field;
		try {
			field = (PdfDictionary)(manager.getObjectIndirect(fieldRef));
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
			kids = (PdfArray)(manager.getObjectIndirect((PdfObject)(fieldHt.get(new PdfName("Kids")))));
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
				
				pjxGetFieldsAddField(manager, fieldList, fieldRef2);

			}
		}
		
	}

	/**
	   This method is provided for compatibility with PJ.
	   @throws PdfFormatException
	   @deprecated
	 */
	public static void pjxUpdateFieldValue(PdfManager manager, PdfDictionary origField,
					       PdfDictionary field, String value)
		throws IOException, com.etymon.pjx.PdfFormatException {

		synchronized (manager) {

			try {

				Map origFieldHtRO = origField.getMap();
				HashMap origFieldHt = new HashMap(origFieldHtRO.size());
				origFieldHt.putAll(origFieldHtRO);
				
				Map fieldHt = field.getMap();
				
				// store old value for use in search/replace within appeareances stream(s)
				PdfString oldValue = (PdfString)(fieldHt.get(new PdfName("V")));
				
				PdfString valueString = new PdfString(value);
				origFieldHt.put(new PdfName("V"), valueString);
				origFieldHt.put(new PdfName("DV"), valueString);
				
				// determine quadding
				PdfInteger q = (PdfInteger)(manager.getObjectIndirect((PdfObject)(fieldHt.get(new PdfName("Q")))));
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
				
				PdfDictionary ap = (PdfDictionary)(manager.getObjectIndirect((PdfObject)(fieldHt.get(new PdfName("AP")))));
				if (ap != null) {
					Map apHtReadOnly = ap.getMap();
					HashMap apHt = new HashMap(apHtReadOnly.size());
					apHt.putAll(apHtReadOnly);
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
						apn = manager.getObjectIndirect(apnRef);
					} else {
						// if it's not an indirect object, let's make it indirect
						apnId = manager.addObject(apnObj);
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
							pjxReplaceTextData(pmVector, oldValue, valueString);
						}
						if (centered) {
							pjxAdjustTextMatrixX(pmVector, rectWidth);
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
						manager.setObject(PjxConvert.toPjxObject(temp), apnId);
					}
				}
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
	private static void pjxReplaceTextData(Vector pmVector, PdfString oldText, PdfString newText)
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
	private static void pjxAdjustTextMatrixX(Vector pmVector, float rectWidth) {
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
	private static void pjxClearTextMatrixX(Vector pmVector) {
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

	/**
	   Returns a clone of a field node such that all inherited
	   attributes of the given field node are made explicit.  For
	   example, if the V key is not defined in the given field
	   node, this method ascends the field tree (via the Parent
	   reference) looking for an ancestor node that does contain a
	   value for the V key; if it finds one, it assigns that value
	   in the cloned (returned) field node.  This is done for all
	   inheritable attributes.
	   This method is provided for compatibility with PJ.  It will
	   be transitioned toward a dedicated field class.
	   @param manager the manager associated with the PDF document.
	   @param d a field node for which inherited attributes are to
	   be retrieved.
	   @return a cloned copy of the given field node with actual
	   values substituted for all inherited attributes.
	   @exception PdfFormatException
	   @deprecated
	*/
	public static PdfDictionary pjxInheritFieldAttributes(PdfManager manager, PdfDictionary d)
		throws IOException, com.etymon.pjx.PdfFormatException {
		synchronized (manager) {
			try {
				PjDictionary node = (PjDictionary)PjxConvert.toPjObject(d);
				PjDictionary newNode;
				try {
					newNode = (PjDictionary)(node.clone());
				}
				catch (CloneNotSupportedException e) {
					throw new com.etymon.pj.exception.InvalidPdfObjectException(e.getMessage());
				}
				Hashtable ht = newNode.getHashtable();
				PjObject parentRef = (PjObject)(newNode.getHashtable().get(PjName.PARENT));
				while (parentRef != null) {
					PjObject parentObj = PjxConvert.toPjObject(
						manager.getObjectIndirect(PjxConvert.toPjxObject(parentRef)));
					if ( ! (parentObj instanceof PjDictionary) ) {
						throw new com.etymon.pj.exception.InvalidPdfObjectException("Ancestor of field node is not a dictionary.");
					}
					PjDictionary parent = (PjDictionary)parentObj;
					pjxInheritFieldAttributesCollapse(PjName.FT, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.V, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.DV, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.FF, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.DR, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.DA, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.Q, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.OPT, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.TOPINDEX, ht, newNode, parent);
					pjxInheritFieldAttributesCollapse(PjName.MAXLEN, ht, newNode, parent);
					parentRef = (PjObject)(parent.getHashtable().get(PjName.PARENT));
				}
				return (PdfDictionary)PjxConvert.toPjxObject(newNode);
			}
			catch (com.etymon.pj.exception.PdfFormatException e) {
				throw new com.etymon.pjx.PdfFormatException(e.getMessage());
			}
			catch (com.etymon.pj.exception.InvalidPdfObjectException f) {
				throw new com.etymon.pjx.PdfFormatException(f.getMessage());
			}
		}
	}
	
	// used exclusively by inheritFieldAttributes()
	/**
	   @deprecated
	 */
	private static void pjxInheritFieldAttributesCollapse(
		PjName name, Hashtable ht, PjDictionary newNode, PjDictionary parent) {
		
		if (ht.get(name) == null) {
			Object obj = parent.getHashtable().get(name);
			if (obj != null) {
				ht.put(name, obj);
			}
		}
	}

}
