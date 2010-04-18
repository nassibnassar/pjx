package com.etymon.pjx.util;

import java.io.*;
import java.nio.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Appends multiple PDF documents together, forming a new PDF
   document.
   @author Nassib Nassar
*/
public class PdfAppender {

	/**
	   A flag used to indicate whether file names should be
	   printed during the appending process.  This is temporary
	   and will be superceded when logging is implemented.
	 */
	protected boolean _printFileNames = false;
	
	protected static PdfObject renumber(PdfObject obj, int offset) throws PdfFormatException {

		if (obj == null) {
			return null;
		}
		
		if (obj instanceof PdfArray) {
			List list = ((PdfArray)obj).getList();
			ArrayList nlist = new ArrayList(list.size());
			for (Iterator t = list.iterator(); t.hasNext(); ) {
				nlist.add( renumber((PdfObject)t.next(), offset) );
			}
			return new PdfArray(nlist);
		}			
		
		if (obj instanceof PdfDictionary) {
			Map map = ((PdfDictionary)obj).getMap();
			HashMap nmap = new HashMap(map.size());
			for (Iterator t = map.keySet().iterator(); t.hasNext(); ) {
				PdfName key = (PdfName)t.next();
				nmap.put( key, renumber((PdfObject)map.get(key), offset) );
			}
			return new PdfDictionary(nmap);
		}

		if (obj instanceof PdfStream) {
			PdfStream s = (PdfStream)obj;
			ByteBuffer bb = s.getBuffer();
			bb.position(0);
			return new PdfStream( (PdfDictionary)renumber(s.getDictionary(), offset), bb );
		}
			
		if (obj instanceof PdfReference) {
			PdfReference r = (PdfReference)obj;
			return new PdfReference(r.getObjectNumber() + offset, 0);
		}
		
		return obj;
		
	}
	
	/**
	   The array of PDF managers.
	*/
	protected PdfManager[] _m;

	/**
	   The current amount to offset (increase) all object numbers
	   by.  The pdfReaderFilter(PdfObject) method renumbers
	   indirect references by adding this value to their object
	   number.
	 */
	protected int _renumber_offset;

	/**
	   Stores whether the {@link #append() append()} method has
	   been used.
	 */
	protected boolean _used;
	
	/**
	   The PDF writer.
	*/
	protected PdfWriter _w;

	/**
	   The class is initialized to read a list of PDF documents
	   (<code>PdfManager</code> objects) in order and to write the
	   resultant document to a specified <code>PdfWriter</code>.
	   The <code>PdfWriter</code> should be newly created (i.e. it
	   should not have been previously used for anything); and
	   after {@link #append() append()} has been called, the
	   <code>PdfWriter</code> should be closed and discarded, and
	   this <code>PdfAppender</code> should be discarded.
	   @param managers the documents to read.
	   @param writer the document to write to.
	   @deprecated Use {@link #PdfAppender(List,
	   PdfWriter) PdfAppender(List, PdfWriter)}.
	 */
	public PdfAppender(PdfManager[] managers, PdfWriter writer) {

		_m = new PdfManager[managers.length];
		System.arraycopy(managers, 0, _m, 0, managers.length);
		
		_w = writer;

		_used = false;

	}

	/**
	   The class is initialized to read a list of PDF documents
	   (<code>PdfManager</code> objects) in order and to write the
	   resultant document to a specified <code>PdfWriter</code>.
	   The <code>PdfWriter</code> should be newly created (i.e. it
	   should not have been previously used for anything); and
	   after {@link #append() append()} has been called, the
	   <code>PdfWriter</code> should be closed and discarded, and
	   this <code>PdfAppender</code> should be discarded.
	   @param managers the documents to read.  This must be a list
	   of <code>PdfManager</code> objects.
	   @param writer the document to write to.
	   @throws PdfFormatException
	 */
	public PdfAppender(List managers, PdfWriter writer) throws PdfFormatException {

		_m = new PdfManager[managers.size()];
		int x = 0;
		for (Iterator t = managers.iterator(); t.hasNext(); ) {
			Object obj = t.next();
			if ( !(obj instanceof PdfManager) ) {
				throw new PdfFormatException("List element is not a PdfManager instance.");
			}
			_m[x++] = (PdfManager)obj;
		}
		
		_w = writer;

		_used = false;

	}

	// needs to be synchronized on the managers
	/**
	   Performs the append operation.  This method can be called
	   only once per instance of this class.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public void append() throws IOException, PdfFormatException {

		if (_used) {
			throw new PdfFormatException("PdfAppender.append() called more than once per instance.");
		}

		_used = true;
		
		PdfManager[] ma = _m;
		PdfWriter w = _w;

		if (ma.length == 0) {
			return;
		}

		int[] pageTreeRootId = new int[ma.length];
		int[] pageTreeRootGen = new int[ma.length];
		PdfDictionary[] pageTreeRoot = new PdfDictionary[ma.length];
		List[] fieldsRef = new List[ma.length];
		List[] fields = new List[ma.length];
		Map newAcroFormMap = null;

		if (_printFileNames) {
			System.out.println(ma[0].getReader().getInput().getName());
		}

		// first copy ma[0] to the output
		long pos = ma[0].writeDocument(w);

		if (ma.length == 1) {
			return;
		}
		
		long prev = ma[0].getStartxref();

		// get the page tree root object
		PdfManager manager = ma[0];
		PdfModifier modifier = new PdfModifier(manager);
		PdfReference pageTreeRootRef = modifier.getPageTreeRootReference();
		pageTreeRootId[0] = pageTreeRootRef.getObjectNumber();
		pageTreeRootGen[0] = pageTreeRootRef.getGenerationNumber();
		pageTreeRoot[0] = modifier.getPageTreeRoot();

		// get the interactive form dictionary
		PdfDictionary catalog = modifier.getCatalog();
		PdfObject acroFormObj = (PdfObject)catalog.getMap().get(new PdfName("AcroForm"));
		PdfDictionary acroForm = (PdfDictionary)ma[0].getObjectIndirect(acroFormObj);
		if (acroForm != null) {
			Map acroFormMap = acroForm.getMap();
			int acroFormMapSize = acroFormMap.size();
			// we only copy the dictionary as a whole if
			// we don't have one yet
			if (newAcroFormMap == null) {
				newAcroFormMap = new HashMap(acroFormMap);
			}
			// now add fields to our running list
			PdfObject fieldsObj = (PdfObject)acroFormMap.get(new PdfName("Fields"));
			PdfArray fa = (PdfArray)ma[0].getObjectIndirect(fieldsObj);
			List fr = new ArrayList();
			List ff = new ArrayList();
			if (fa != null) {
				for (Iterator t = fa.getList().iterator(); t.hasNext(); ) {
					PdfReference f = (PdfReference)t.next();
					fr.add( f );
					ff.add( ma[0].getObjectIndirect(f) );
				}
			}
			fieldsRef[0] = fr;
			fields[0] = ff;
			
		}

		int pageCount;
		PdfInteger countObj =
			(PdfInteger)manager.getObjectIndirect((PdfObject)(
								      pageTreeRoot[0].getMap().get(new PdfName("Count"))));
		if (countObj != null) {
			pageCount = countObj.getInt();
		} else {
			pageCount = 0;
		}
		
		_renumber_offset = manager.getXrefTableSize();
		
		// next append the remaining documents
		for (int mx = 1; mx < ma.length; mx++) {

			PdfManager m = ma[mx];

			if (_printFileNames) {
				System.out.println(m.getReader().getInput().getName());
			}
			
			// first extract needed information, before we
			// renumber all of the objects.  we need the
			// object number of the root of the page tree.
			
			manager = m;
			modifier = new PdfModifier(manager);
			pageTreeRootRef = modifier.getPageTreeRootReference();
			pageTreeRootId[mx] = pageTreeRootRef.getObjectNumber() +
				_renumber_offset;
			pageTreeRootGen[mx] = pageTreeRootRef.getGenerationNumber();
			// we delay setting pageTreeRoot[rax] and
			// using it to get the number of pages until
			// later when we can get the renumbered
			// version of the page tree root
			
			// get the interactive form dictionary
			catalog = modifier.getCatalog();
			acroFormObj = (PdfObject)catalog.getMap().get(new PdfName("AcroForm"));
			acroForm = (PdfDictionary)m.getObjectIndirect(acroFormObj);
			if (acroForm != null) {
				Map acroFormMap = acroForm.getMap();
				int acroFormMapSize = acroFormMap.size();
				// we only copy the dictionary as a whole if
				// we don't have one yet
				if (newAcroFormMap == null) {
					newAcroFormMap = new HashMap(acroFormMapSize);
					for (Iterator t = acroFormMap.keySet().iterator(); t.hasNext(); ) {
						PdfName key = (PdfName)t.next();
						newAcroFormMap.put( key,
								    renumber((PdfObject)acroFormMap.get(key),
									     _renumber_offset) );
					}
				}
				// now add fields to our running list
				PdfObject fieldsObj = (PdfObject)acroFormMap.get(new PdfName("Fields"));
				PdfArray fa = (PdfArray)m.getObjectIndirect(fieldsObj);
				List fr = new ArrayList();
				List ff = new ArrayList();
				if (fa != null) {
					for (Iterator t = fa.getList().iterator(); t.hasNext(); ) {
						PdfReference f = (PdfReference)t.next();
						fr.add( renumber(f, _renumber_offset) );
						ff.add( renumber(m.getObjectIndirect(f), _renumber_offset) );
					}
				}
				fieldsRef[mx] = fr;
				fields[mx] = ff;
			}
			
			// next read all the objects, renumber them,
			// and write them to the output

			int xtSize = m.getXrefTableSize();
			int nxtSize = xtSize + _renumber_offset;

			long[] index = new long[nxtSize];
			int[] generation = new int[nxtSize];
			byte[] usage = new byte[nxtSize];
			index[0] = XrefTable.ENTRY_FREE;
			generation[0] = 65535;
			usage[0] = XrefTable.ENTRY_FREE;
			
			for (int x = 1; x < xtSize; x++) {
				
				PdfObject obj = m.getObject(x);
				
				if (obj != null) {

					obj = renumber(obj, _renumber_offset);
					
					index[_renumber_offset + x] = pos;
					generation[_renumber_offset + x] = 0;
					usage[_renumber_offset + x] = XrefTable.ENTRY_IN_USE;

					
					pos += w.writeObjectIndirect(obj, x + _renumber_offset, 0);

					if ((x + _renumber_offset) == pageTreeRootId[mx]) {
						pageTreeRoot[mx] = (PdfDictionary)obj;
						
						// now we can get the number of pages
						countObj =
							(PdfInteger)manager.getObjectIndirect((PdfObject)(
												      pageTreeRoot[mx].getMap().get(new PdfName("Count"))));
						if (countObj != null) {
							pageCount += countObj.getInt();
						}
						
					}
 				} else {
					
 					generation[_renumber_offset + x] = 0;
 					usage[_renumber_offset + x] = XrefTable.ENTRY_FREE;
					
 				}

			}
			
			// finally, write the xref table and trailer

			PdfDictionary trailer = m.getTrailerDictionary();
			Map trailerMap = trailer.getMap();

			HashMap ntrailerMap = new HashMap(trailerMap);
			
			ntrailerMap.put(new PdfName("Size"), new PdfInteger(_renumber_offset + nxtSize));
			ntrailerMap.put(new PdfName("Prev"), new PdfLong(prev));

			prev = pos;
			
			PdfDictionary ntrailer = new PdfDictionary(ntrailerMap);
			
			XrefTable nxt = new XrefTable(index, generation, usage, ntrailer);

			pos += w.writeXrefTable(nxt, pos);

			_renumber_offset = nxtSize;
			
		}

		// write the old page tree roots and field
		// dictionaries with new parent values

		int newPageTreeRootId = _renumber_offset;
		int newFieldsId = _renumber_offset + 1;
		int newCatalogId = _renumber_offset + 1 + fields.length;
		int xtSize = _renumber_offset + 2 + fields.length;
		
		long[] index = new long[xtSize];
		int[] generation = new int[xtSize];
		byte[] usage = new byte[xtSize];
		Arrays.fill(usage, XrefTable.ENTRY_UNDEFINED);
		index[0] = 0;
		generation[0] = 65535;
		usage[0] = XrefTable.ENTRY_FREE;

		for (int x = 0; x < pageTreeRoot.length; x++) {
			index[pageTreeRootId[x]] = pos;
			generation[pageTreeRootId[x]] = pageTreeRootGen[x];
			usage[pageTreeRootId[x]] = XrefTable.ENTRY_IN_USE;

			// update parent value
			PdfDictionary d = pageTreeRoot[x];
			Map map = d.getMap();
			HashMap nmap = new HashMap(map);
			nmap.put(new PdfName("Parent"), new PdfReference(newPageTreeRootId, 0));

			pos += w.writeObjectIndirect(new PdfDictionary(nmap),
						     pageTreeRootId[x], pageTreeRootGen[x]);
		}

		for (int y = 0; y < fields.length; y++) {
			if (fields[y] != null) {
				int fieldsSize = fields[y].size();
				for (int x = 0; x < fieldsSize; x++) {
					PdfReference ref = (PdfReference)fieldsRef[y].get(x);
					int id = ref.getObjectNumber();
					int gen = ref.getGenerationNumber();
					index[id] = pos;
					generation[id] = gen;
					usage[id] = XrefTable.ENTRY_IN_USE;
					
					// update parent value
					PdfDictionary d = (PdfDictionary)fields[y].get(x);
					Map map = d.getMap();
					HashMap nmap = new HashMap(map);
					nmap.put(new PdfName("Parent"), new PdfReference(newFieldsId + y, 0));
					
					pos += w.writeObjectIndirect(new PdfDictionary(nmap),
								     id, gen);
				}
			}
		}
		
		// write the new page tree root, which contains the
		// root from each document

		HashMap rootMap = new HashMap();
		rootMap.put(new PdfName("Type"), new PdfName("Pages"));
		rootMap.put(new PdfName("Count"), new PdfInteger(pageCount));
		ArrayList kids = new ArrayList(pageTreeRoot.length);
		for (int x = 0; x < pageTreeRoot.length; x++) {
			kids.add( new PdfReference(pageTreeRootId[x],
						   pageTreeRootGen[x]) );
		}
		rootMap.put(new PdfName("Kids"), new PdfArray(kids));

		index[newPageTreeRootId] = pos;
		generation[newPageTreeRootId] = 0;
		usage[newPageTreeRootId] = XrefTable.ENTRY_IN_USE;
		
		pos += w.writeObjectIndirect(new PdfDictionary(rootMap), newPageTreeRootId, 0);

		// write the new fields roots, which contain all the
		// fields

		List fieldRootList = new ArrayList(fields.length);
		
		for (int x = 0; x < fields.length; x++) {

			if (fields[x] != null) {
			
				rootMap = new HashMap();
				kids = new ArrayList(fields[x].size());
				for (Iterator t = fieldsRef[x].iterator(); t.hasNext(); ) {
					PdfReference ref = (PdfReference)t.next();
					kids.add(ref);
				}
				rootMap.put(new PdfName("Kids"), new PdfArray(kids));
				
				rootMap.put(new PdfName("T"), new PdfString("A" + x));
				
				int n = newFieldsId + x;
				index[n] = pos;
				generation[n] = 0;
				usage[n] = XrefTable.ENTRY_IN_USE;
				
				pos += w.writeObjectIndirect(new PdfDictionary(rootMap), n, 0);

				fieldRootList.add(new PdfReference(n, 0));
			}

		}

		// build the interactive form dictionary for the new
		// catalog

		Map buildAcroFormMap;
		if (newAcroFormMap != null) {
			buildAcroFormMap = new HashMap(newAcroFormMap);
			buildAcroFormMap.put(new PdfName("Fields"), new PdfArray(fieldRootList));
		} else {
			buildAcroFormMap = null;
		}
		
		// write the new catalog

		HashMap catalogMap = new HashMap();
		catalogMap.put(new PdfName("Type"), new PdfName("Catalog"));
		catalogMap.put(new PdfName("Pages"), new PdfReference(newPageTreeRootId, 0));
		if (buildAcroFormMap != null) {
			catalogMap.put(new PdfName("AcroForm"), new PdfDictionary(buildAcroFormMap));
		}
			       
		index[newCatalogId] = pos;
		generation[newCatalogId] = 0;
		usage[newCatalogId] = XrefTable.ENTRY_IN_USE;
		
		pos += w.writeObjectIndirect(new PdfDictionary(catalogMap), newCatalogId, 0);

		// write the final xref table and trailer
		
		HashMap ntrailerMap = new HashMap();
		
		ntrailerMap.put(new PdfName("Size"), new PdfInteger(xtSize));
		ntrailerMap.put(new PdfName("Prev"), new PdfLong(prev));
		ntrailerMap.put(new PdfName("Root"), new PdfReference(newCatalogId, 0));
		
		PdfDictionary ntrailer = new PdfDictionary(ntrailerMap);
		
		XrefTable nxt = new XrefTable(index, generation, usage, ntrailer);
		
		pos += w.writeXrefTable(nxt, pos);

	}

	/**
	   Appends multiple PDF documents together using this class.
	   The documents are specified with a list of file names; the
	   last indicating the output file and the others indicating
	   the input files.  The input files are appended in the order
	   they are specified within the list.
	   @param args the list of file names.  <b>Note that the last
	   file in this list (<code>args[args.length - 1]</code>) is
	   overwritten with the resultant PDF document.</b>
	   @throws IOException
	   @throws PdfFormatException
	 */
        public static void main(String[] args) throws IOException, PdfFormatException {

		if (args.length < 2) {
			System.err.println(
				"Usage:  java com.etymon.pjx.util.PdfAppender [input1.pdf] [input2.pdf] [...] [output.pdf]");
			return;
		}

		List m = new ArrayList(args.length - 1);

		for (int x = 0; x < args.length - 1; x++) {
			try {
				m.add( new PdfManager(new PdfReader(new PdfInputFile(new File(args[x])))) );
			} catch (PdfFormatException e) {
				throw new PdfFormatException(args[x] + ": " + e.getMessage(), e.getOffset());
			}
		}
		
		PdfWriter w = new PdfWriter(new File(args[args.length - 1]));

		PdfAppender a = new PdfAppender(m, w);
		a._printFileNames = true;
		a.append();

		w.close();

	}	

}
