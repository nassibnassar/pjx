/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.util.*;

/**
   Represents the PDF cross-reference table and associated trailer
   dictionary.  This class is immutable.
   @author Nassib Nassar
*/
public class XrefTable {

	/**
	   The array of generation values.
	 */
	protected int[] _generation;
	
	/**
	   The array of index values.  Each value represents either a
           byte offset (if in-use) or the next free object number (if
           free).
	 */
	protected long[] _index;

	/**
	   The array of sorted index values.  This contains only
	   in-use entries, to be used for binary searching and
	   locating the offset of the next object in the file.
	 */
	protected long[] _index_sorted;

	/**
	   The list of startxref values associated with this
	   cross-reference table.
	 */
	protected List _startxrefs;
	
	/**
	   The trailer dictionary associated with this cross-reference
	   table.
	 */
	protected PdfDictionary _trailer;
	
	/**
	   The array of usage values.  Each values is ENTRY_FREE,
	   ENTRY_IN_USE, or ENTRY_UNDEFINED.
	 */
	protected byte[] _usage;

	/**
	   This indicates that an entry is free.
	 */
        public static final byte ENTRY_FREE = 1;
        
	/**
	   This indicates that an entry is in-use.
	 */
        public static final byte ENTRY_IN_USE = 2;
        
	/**
	   This indicates that an entry is undefined.
	 */
	public static final byte ENTRY_UNDEFINED = 0;  // do not change
        
	/**
	   A protected constructor intended to be called only from
	   {@link #wrap(long[], int[], byte[], PdfDictionary)
	   wrap(long[], int[], byte[], PdfDictionary)}.
	 */
	protected XrefTable() {
	}
	
	/**
	   Constructs a cross-reference table from a set of arrays and
	   a trailer dictionary.
	   @param index the array of index values.  Each value
           represents either a byte offset (if in-use) or the next
           free object number (if free).
	   @param generation the array of generation values.
	   @param usage the array of usage values.  Each value is
	   {@link #ENTRY_FREE ENTRY_FREE}, {@link #ENTRY_IN_USE
	   ENTRY_IN_USE}, or {@link #ENTRY_UNDEFINED ENTRY_UNDEFINED}.
	   @param trailerDictionary the trailer dictionary.
	   @throws PdfFormatException
	 */
	public XrefTable(long[] index, int[] generation, byte[] usage,
			 PdfDictionary trailerDictionary) throws PdfFormatException {

		if ( (index.length != generation.length) &&
		     (index.length != usage.length) ) {
			throw new PdfFormatException("Xref arrays are not the same length.");
		}
		_index = new long[index.length];
		_generation = new int[generation.length];
		_usage = new byte[usage.length];
		System.arraycopy(index, 0, _index, 0, index.length);
		System.arraycopy(generation, 0, _generation, 0, generation.length);
		System.arraycopy(usage, 0, _usage, 0, usage.length);
		_startxrefs = new ArrayList();
		createSortedIndexArray();
		
		_trailer = trailerDictionary;
	}

	/**
	   Returns a shallow copy of this instance.
	   @return a clone of this instance.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	protected void createSortedIndexArray() {

		// determine number of entries that are in use
		int size = 0;
		for (int x = 0; x < _usage.length; x++) {
			if (_usage[x] == ENTRY_IN_USE) {
				size++;
			}
		}

		_index_sorted = new long[size + _startxrefs.size()];
		int y = 0;
		// add index values
		for (int x = 0; x < _usage.length; x++) {
			if (_usage[x] == ENTRY_IN_USE) {
				_index_sorted[y++] = _index[x];
			}
		}
		// add startxref values
		for (Iterator p = _startxrefs.iterator(); p.hasNext(); ) {
			_index_sorted[y++] = ((Long)p.next()).longValue();
		}
		
		Arrays.sort(_index_sorted);
		
	}

	/**
	   Returns an offset estimated to be relatively close to the
	   end of the object (specified by object number).  The offset
	   will be no earlier than the end of the object.
	   @param n the specified object number.
	   @return the index value, or -1 if the specified object
	   number corresponds to the last object in the document.  If
	   the object number does not correspond to an in-use entry,
	   then -1 is returned.
	 */
	public long estimateObjectEnd(int n) {

		if (_usage[n] != ENTRY_IN_USE) {
			return -1;
		}

		long start = _index[n];
		
		int x = Arrays.binarySearch(_index_sorted, start);

		while ( (x < _index_sorted.length) && (_index_sorted[x] == start) ) {
			x++;
		}
		
		if (x < _index_sorted.length) {
			return _index_sorted[x];
		} else {
			return -1;
		}
		
	}
	
	/**
	   Returns the generation value for a specified object.
	   @param n the object number.
	   @return the generation value.
	 */
	public int getGeneration(int n) {
		return _generation[n];
	}

	/**
	   Returns the array of generation values.  The calling method
	   must not modify this array unless it guarantees that this
	   object exists in no other thread, in order to comply with
	   the immutability requirement of this class.
	   @return the array of generation values.
	 */
	public int[] getGenerationArray() {
		int[] a = new int[_generation.length];
		System.arraycopy(_generation, 0, a, 0, _generation.length);
		return a;
	}

	/**
	   Returns the index value for a specified object.
	   @param n the object number.
	   @return the index value.
	 */
	public long getIndex(int n) {
		return _index[n];
	}

	/**
	   Returns the array of index values.  The calling method must
	   not modify this array unless it guarantees that this object
	   exists in no other thread, in order to comply with the
	   immutability requirement of this class.
	   @return the array of index values.
	 */
	public long[] getIndexArray() {
		long[] a = new long[_index.length];
		System.arraycopy(_index, 0, a, 0, _index.length);
		return a;
	}

	/**
	   Returns the list of startxref values associated with this
	   cross-reference table.  The calling method must not modify
	   this list unless it guarantees that this object exists in
	   no other thread, in order to comply with the immutability
	   requirement of this class.
	   @return the list of startxref values (as
           <code>Long</code> objects.
	*/
	protected List getStartxrefList() {
		return _startxrefs;
	}

	/**
	   Returns the trailer dictionary associated with this
	   cross-reference table.
	   @return the trailer dictionary.
	*/
	public PdfDictionary getTrailerDictionary() {
		return _trailer;
	}

	/**
	   Returns the usage value for a specified object.
	   @param n the object number.
	   @return the usage value.
	 */
	public byte getUsage(int n) {
		return _usage[n];
	}

	/**
	   Returns the array of usage values.  The calling method must
	   not modify this array unless it guarantees that this object
	   exists in no other thread, in order to comply with the
	   immutability requirement of this class.
	   @return the array of usage values.
	 */
	public byte[] getUsageArray() {
		byte[] a = new byte[_usage.length];
		System.arraycopy(_usage, 0, a, 0, _usage.length);
		return a;
	}

	/**
	   Returns the number of entries in this cross-reference
	   table.
	   @return the number of entries.
	 */
	public int size() {
		return _usage.length;
	}
	
	/**
	   Returns the cross-reference table, associated trailer
	   dictionary, and a complete PDF trailer as a string in PDF
	   format.
	   @return the PDF string.
	 */
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			PdfWriter w = new PdfWriter(baos);
			writePdf(w, 0);
			w.close();
			baos.close();
		}
		catch (IOException e) {
			return null;
		}
		return baos.toString();
	}

	/**
	   Returns the array of generation values.  The calling method
	   must ensure that the array is never externally modified, in
	   order to meet the immutability requirement of this class.
	   @return the array of generation values.
	 */
	protected int[] unwrapGenerationArray() {
		return _generation;
	}

	/**
	   Returns the array of index values.  The calling method must
	   ensure that the array is never externally modified, in
	   order to meet the immutability requirement of this class.
	   @return the array of index values.
	 */
	protected long[] unwrapIndexArray() {
		return _index;
	}

	/**
	   Returns the array of usage values.  The calling method must
	   ensure that the array is never externally modified, in
	   order to meet the immutability requirement of this class.
	   @return the array of usage values.
	*/
	protected byte[] unwrapUsageArray() {
		return _usage;
	}
	
	/**
	   A factory for fast construction of this class.  The
	   constructed object will be a wrapper around the specified
	   objects.  The calling method must ensure that the arrays
	   are never externally modified, in order to meet the
	   immutability requirement of this class.  It must also
	   ensure that all three arrays have the same length.  It is
	   also the calling method's responsibility to call {@link
	   #createSortedIndexArray() createSortedIndexArray()} before
	   the instance will be used.
	   @param index the array of index values.
	   @param generation the array of generation values.
	   @param usage the array of usage values.
	   @param trailerDictionary the trailer dictionary.
	   @return the constructed object.
	 */
	protected static XrefTable wrap(long[] index, int[] generation, byte[] usage, PdfDictionary trailerDictionary) {
		XrefTable xt = new XrefTable();
		xt._index = index;
		xt._generation = generation;
		xt._usage = usage;
		xt._trailer = trailerDictionary;
		xt._startxrefs = new ArrayList();
		return xt;
	}
	
	/**
	   Writes the cross-reference table, associated trailer
	   dictionary, and a complete PDF trailer in PDF format.
	   Before writing the cross-reference table, the free entries
	   are combined into a linked list as required by the PDF
	   specification.
	   @param w the <code>PdfWriter</code> to write to.
	   @param startxref the byte offset within the output file
	   where the cross-reference table begins.
	   @return the number of bytes written by this method.
	   @throws IOException
	 */
	protected int writePdf(PdfWriter w, long startxref) throws IOException {

		xrefGenerateFreeList();
		
		DataOutputStream dos = w.getDataOutputStream();
		
		String s, t;
		
		dos.writeBytes("xref\n");
		int count = 5;
		
		int x, xx;
		x = 0;
		while (x < size()) {
			
			if (_usage[x] != ENTRY_UNDEFINED) {
				
				xx = x + 1;
				while ( (xx < size()) && (_usage[xx] != ENTRY_UNDEFINED) ) {
					xx++;
				}
				
				s = Integer.toString(x);
				dos.writeBytes(s);
				dos.write(' ');
				t = Integer.toString(xx - x);
				dos.writeBytes(t);
				dos.write('\n');
				count += s.length() + t.length() + 2;
				
				while (x < xx) {
					// write index
					s = Long.toString(_index[x]);
					int y = s.length();
					for (int z = y; z < 10; z++) {
						dos.write('0');
					}
					dos.writeBytes(s);
					dos.write(' ');
					// write generation
					s = Integer.toString(_generation[x]);
					y = s.length();
					for (int z = y; z < 5; z++) {
						dos.write('0');
					}
					dos.writeBytes(s);
					dos.write(' ');
					if (_usage[x] == ENTRY_IN_USE) {
						dos.write('n');
					} else {
						dos.write('f');
					}
					dos.writeBytes(" \n");
					count += 20;
					x++;
				}
			}
			x++;
		}
		
		dos.writeBytes("trailer\n");
		count += 8;

		count += _trailer.writePdf(w, false);
		
		s = "\nstartxref\n" + Long.toString(startxref) + "\n%%EOF\n";
		dos.writeBytes(s);
		return count + s.length();
	}

	/**
	   Combines the free elements of this cross-reference table
	   into a linked list as required by the PDF specification.
	 */
	protected void xrefGenerateFreeList() {
		int lastFree = 0;
		long[] index = _index;
		byte[] usage = _usage;
		for (int x = usage.length - 1; x >= 0; x--) {
			if (usage[x] == ENTRY_FREE) {
				index[x] = lastFree;
				lastFree = x;
			}
		}
	}
	
}
