/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.regex.*;

/**
   Reads a PDF document.  Most applications do not need to access
   methods in this class but should instead go through {@link
   PdfManager PdfManager}.  This class is synchronized.
   @author Nassib Nassar
*/
public final class PdfReader {

	protected PdfInput _pdfInput;

	/**
	   Returns the <code>PdfInput</code> instance associated with
	   this document.
	*/
	public PdfInput getInput() {
		synchronized (this) {

			return _pdfInput;

		}
	}
	
	protected PdfInput getPdfInput() {
		return _pdfInput;
	}
	
	/**
	   A placeholder used by the PDF parser to mark the end of an
	   array.
	*/
	protected class ArrayEnd extends ParserObject { }
	
	/**
	   A placeholder used by the PDF parser to mark the end of a
	   dictionary.
	 */
	protected class DictionaryEnd extends ParserObject { }
		
	/**
	   A placeholder used by the PDF parser to mark the end of a
	   dictionary that is also followed by a stream.
	 */
	protected class DictionaryEndStream extends ParserObject { }
		
	/**
	   The superclass of inner classes used by this
	   <code>PdfReader</code> to mark positions while parsing PDF
	   objects.
	*/
	protected class ParserObject extends PdfObject {

		protected int writePdf(PdfWriter w, boolean spacing) throws IOException {
			return 0;
		}
		
	}
	
	/**
	   The regular expression that matches a PDF header.
	*/
	protected static Pattern _patHeader = Pattern.compile("^%(!PS-Adobe-\\d\\.\\d )?PDF-\\d\\.\\d" +
							      PdfReader.REGEX_EOL);

	/**
	   The regular expression that matches the begining of an
	   indirect object (specifically, the object number and
	   generation number followed by "obj").
	*/
	protected static final Pattern _patObjIntro = Pattern.compile(
		PdfReader.REGEX_WHITESPACE + "*\\d+" +
		PdfReader.REGEX_WHITESPACE + "+\\d+" +
		PdfReader.REGEX_WHITESPACE + "+obj" +
		PdfReader.REGEX_STOP);

	/**
	   The regular expression that matches a PDF (direct) object.
	 */
	protected static final Pattern _patPdfObject = Pattern.compile(

		"(" +

		// Null 2
		"(" + PdfReader.REGEX_WHITESPACE + "*null" + PdfReader.REGEX_STOP + ")|" +

		// Reference 12
		"(" + PdfReader.REGEX_WHITESPACE + "*\\d+" + PdfReader.REGEX_WHITESPACE + "\\d+" + PdfReader.REGEX_WHITESPACE + "R" + PdfReader.REGEX_STOP + ")|" +

		// Boolean 30 (35 = true)
		"(" + PdfReader.REGEX_WHITESPACE + "*((true)|(false))" + PdfReader.REGEX_STOP + ")|" +

		// Integer 43
		"(" + PdfReader.REGEX_WHITESPACE + "*(\\+|\\-)?\\d+" + PdfReader.REGEX_STOP + ")|" +

		// Float 54
		"(" + PdfReader.REGEX_WHITESPACE + "*(\\+|\\-)?((\\d*\\.\\d+)|(\\d+\\.))" + PdfReader.REGEX_STOP + ")|" +

		// String 68
		"(" + PdfReader.REGEX_WHITESPACE + "*((\\()|(<[^<]))" + PdfReader.REGEX_ANY_CHAR + ")|" +
		
		// Name 76
		"(" + PdfReader.REGEX_WHITESPACE +
							  "*/((#\\d\\d)|(" + PdfReader.REGEX_REGULAR + "))*" +
							  PdfReader.REGEX_STOP + ")|" +

		// Dictionary begin 89
		"(" + PdfReader.REGEX_WHITESPACE + "*<<" + PdfReader.REGEX_ANY_CHAR + ")|" +

		// Dictionary end / Stream begin 94
		"(" + PdfReader.REGEX_WHITESPACE + "*>>" + PdfReader.REGEX_WHITESPACE + "*stream((\\r\\n)|\\n|\\r)"  + ")|" +
		// We accept a solitary '\r' after the "stream"
		// keyword even though the PDF specification does not
		// allow it, because of a sample file << /Creator
		// (Adobe Illustrator_TM_ 7.0) /Producer (Acrobat PDF
		// File Format 1.1 for Macintosh) >> that used it.
		// The only harm in accepting it is that output from
		// such a non-compliant program might be read
		// incorrectly by PJX in the rare case that the stream
		// being with '\n'.  That is no harm since without
		// recognizing '\r' we couldn't read it anyway
		// (causing an exception to be thrown at some point
		// later during processing of the PDF document).

		// Dictionary end 105
		"(" + PdfReader.REGEX_WHITESPACE + "*>>" + PdfReader.REGEX_ANY_CHAR + ")|" +

		// Array begin 110
		"(" + PdfReader.REGEX_WHITESPACE + "*\\[" + PdfReader.REGEX_ANY_CHAR + ")|" +

		// Array end 115
		"(" + PdfReader.REGEX_WHITESPACE + "*\\]" + PdfReader.REGEX_ANY_CHAR + ")" +

		")"
		
		);

	/**
	   The regular expression that matches a startxref section.
	*/
	protected static final Pattern _patStartxref = Pattern.compile(
		PdfReader.REGEX_EOL + "startxref" +
		PdfReader.REGEX_WHITESPACE +
		"+\\d+" + PdfReader.REGEX_WHITESPACE);
	
	/**
	   The regular expression that matches the beginning of an
	   xref section (specifically, the "xref" key word).
	*/
	protected static final Pattern _patXref = Pattern.compile("xref" + PdfReader.REGEX_WHITESPACE + "+");

	/**
	   The regular expression that matches the introduction to a
	   subsection of an xref section (specifically, an integer
	   pair) or the "trailer" key word.
	*/
	protected static final Pattern _patXrefSub = Pattern.compile(
		PdfReader.REGEX_WHITESPACE + "*((\\d+ \\d+)|(trailer))" + PdfReader.REGEX_WHITESPACE + "+");

	/**
	   The regular expression that matches an entire xref table
	   section, including the "trailer" key word.
	*/
	protected static final Pattern _patXrefTable = Pattern.compile(
		"xref" + PdfReader.REGEX_WHITESPACE + "*" +
		PdfReader.REGEX_EOL + "[^t]*" + "trailer" + 
		PdfReader.REGEX_WHITESPACE + "+");

	/**
	   The regular expression that matches an entire xref table
	   section, including the "trailer" key word.
	*/
	protected static final Pattern _patXrefEof = Pattern.compile(
		PdfReader.REGEX_ANY_CHAR + "*" + PdfReader.REGEX_WHITESPACE + "startxref" + PdfReader.REGEX_WHITESPACE);

	/**
           A <code>PdfName</code> object representing the name
           <code>Length</code>.
	*/
	protected static final PdfName PDFNAME_LENGTH = new PdfName("Length");

	/**
           A <code>PdfName</code> object representing the name
           <code>Prev</code>.
	*/
	protected static final PdfName PDFNAME_PREV = new PdfName("Prev");

	/**
           A <code>PdfName</code> object representing the name
           <code>Size</code>.
	*/
	protected static final PdfName PDFNAME_SIZE = new PdfName("Size");

	/**
	   The regular expression that matches literally any character.
	*/
	protected static final String REGEX_ANY_CHAR = "[\\x00-\\xFF]";
	
	/**
	   The regular expression that matches a comment in PDF.
	*/
	protected static final String REGEX_COMMENT = "(%[^" + PdfReader.REGEX_EOL + "]*" + PdfReader.REGEX_EOL + ")";

	/**
	   The regular expression that matches a delimiter in PDF.
	*/
	protected static final String REGEX_DELIMITER = "[\\(\\)<>\\[\\]\\{\\}/%]";

	/**
	   The regular expression that matches an end-of-line (EOL)
	   marker in PDF.
	*/
	protected static final String REGEX_EOL = "(\\r|\\n|(\\r\\n))";

	/**
	   The regular expression that matches a regular character in PDF.
	*/
	protected static final String REGEX_REGULAR = "[^\\x00\\t\\n\\f\\r \\(\\)<>\\[\\]\\{\\}/%]";
	
	/**
	   The regular expression that matches a white-space or
	   delimiter (stopping syntactic entities) in PDF.
	*/
	protected static final String REGEX_STOP = "(" + PdfReader.REGEX_WHITESPACE + "|[\\(\\)<>\\[\\]\\{\\}/])";

	/**
	   The regular expression that matches general white-space in PDF.
	*/
	protected static final String REGEX_WHITESPACE = "([\\x00\\t\\n\\f\\r ]|" + PdfReader.REGEX_COMMENT + ")";

	/**
	   Number of times to try scanning for startxref.  Each time
	   the parser will back up to a point (STARTXREF_RETRY_SCAN)
	   bytes before the previous time.
	*/
	protected static final int STARTXREF_RETRY_COUNT = 25;

	/**
           The number of bytes from the end of a PDF document at which to
           start scanning for startxref.
	*/
	protected static final int STARTXREF_RETRY_SCAN = 40;

        /**
           Creates a reader for a PDF document to be read from a
           <code>PdfInput</code> source.
           @param pdfInput the source to read the PDF document from.
        */
	public PdfReader(PdfInput pdfInput) {

		_pdfInput = pdfInput;

	}
	
        /**
           Closes the PDF document and releases any system resources
           associated with it.
           @throws IOException
        */
        public void close() throws IOException {
		synchronized (this) {

			_pdfInput = null;

		}
        }

	/**
	   Parses and returns a PDF object from the input source.  The
	   object is filtered through <code>PdfReaderFilter</code>.
	   It is possible for this method to return <code>null</code>
	   if the filtering method discards all objects.  This method
	   is intended to be called from <code>readObject()</code>
	   which advanced the buffer position past introduction if the
	   object is indirect.
	   @param start the offset where the object starts.
	   @param end the offset where the object ends.
	   @param cbuf the character buffer cached from
	   <code>readObject()</code>.
	   @param xt the cross-reference table; used for resolving
	   indirect references.
	   @throws PdfFormatException
        */
        protected PdfObject parseObject(long start, long end, CharBuffer cbuf,
					XrefTable xt) throws IOException, PdfFormatException {
		Matcher m;
		
		m = _patPdfObject.matcher(cbuf);
		if (m.lookingAt()) {

			if (m.group(2) != null) {
				cbuf.position(cbuf.position() + m.end() - 1);
				return PdfNull.valueOf();
			}
			
			if (m.group(12) != null) {
				String s = m.group();
				s = s.substring(0, s.length() - 1).trim();
				cbuf.position(cbuf.position() + m.end() - 1);
				String[] sp = s.split(PdfReader.REGEX_WHITESPACE);
				return new PdfReference(
					Integer.parseInt(sp[0]),
					Integer.parseInt(sp[1]) );
			}
			
			if (m.group(30) != null) {
				PdfBoolean bool = PdfBoolean.valueOf(m.group(35) != null);
				cbuf.position(cbuf.position() + m.end() - 1);
				return bool;
			}
			
			if (m.group(43) != null) {
				String s = m.group();
				s = s.substring(0, s.length() - 1).trim();
				cbuf.position(cbuf.position() + m.end() - 1);
					long n = Long.parseLong(s);
					if ( (n >= Integer.MIN_VALUE) &&
					     (n <= Integer.MAX_VALUE) ) {
						return new PdfInteger( (int)n );
					} else {
						return new PdfLong(n);
					}
			}

			if (m.group(54) != null) {
				String s = m.group();
				s = s.substring(0, s.length() - 1).trim();
				cbuf.position(cbuf.position() + m.end() - 1);
				return new PdfFloat(Float.parseFloat(s));
			}
			
			if (m.group(68) != null) {
				cbuf.position( cbuf.position() + m.start() );
				return new PdfString( PdfString.pdfToString(cbuf) );
			}
			
			if (m.group(76) != null) {
				String s = m.group();
				s = s.substring(0, s.length() - 1).trim();
				cbuf.position(cbuf.position() + m.end() - 1);
				return new PdfName(PdfName.pdfToString(s));
			}
			
			if (m.group(89) != null) {
				cbuf.position(cbuf.position() + m.end() - 1);
				HashMap h = new HashMap();
				int done = 0;
				PdfObject streamLength = null;
				do {
					PdfObject key, value;
					key = parseObject(start, end, cbuf, xt);
					if (key instanceof DictionaryEnd) {
						done = 1;
						break;
					}
					if (key instanceof DictionaryEndStream) {
						done = 2;
						break;
					}
					if (key.equals(PDFNAME_LENGTH)) {
						streamLength = parseObject(start, end, cbuf, xt);
						value = streamLength;
					} else {
						value = parseObject(start, end, cbuf, xt);
					}
					if ( (key != null) && (value != null) ) {
						h.put(key, value);
					}
				} while (done == 0);
				if (done == 1) { // DictionaryEnd
					return PdfDictionary.wrap(h);
				} else { // DictionaryEndStream
					PdfObject obj = streamLength;
					if (obj instanceof PdfReference) { // get indirect reference
						int save = cbuf.position();
						int streamLengthId = ((PdfReference)obj).getObjectNumber();
						long s = xt.getIndex(streamLengthId);
						long e = xt.estimateObjectEnd(streamLengthId);
						obj = readObject(s, e, true, xt);
						cbuf.position(save);
					}
					if ( !(obj instanceof PdfInteger) ) {
						throw new PdfFormatException(
							"Valid Length value not found in stream dictionary.",
							cbuf.position());
					}
					int len = ((PdfInteger)obj).getInt();
					PdfDictionary d = PdfDictionary.wrap(h);

					ByteBuffer bbuf = _pdfInput.readBytes(start, end);
					ByteBuffer bb = ByteBuffer.allocateDirect(len);
					bbuf.position(cbuf.position());
					bbuf.limit(cbuf.position() + len);
					bb.put(bbuf);
					bbuf.limit(bbuf.capacity());
					
					return PdfStream.wrap(d, bb);
				}
			}
			
			if (m.group(94) != null) {
				cbuf.position(cbuf.position() + m.end());
				return new DictionaryEndStream();
			}
			
			if (m.group(105) != null) {
				cbuf.position(cbuf.position() + m.end() - 1);
				return new DictionaryEnd();
			}
			
			if (m.group(110) != null) {
				cbuf.position(cbuf.position() + m.end() - 1);
				ArrayList a = new ArrayList();
				boolean done = false;
				do {
					Object value;
					value = parseObject(start, end, cbuf, xt);
					if (value instanceof ArrayEnd) {
						done = true;
						break;
					}
					if (value != null) {
						a.add(value);
					}
				} while (!done);
				return PdfArray.wrap(a);
			}
			
			if (m.group(115) != null) {
				cbuf.position(cbuf.position() + m.end() - 1);
				return new ArrayEnd();
			}
			
		}
		throw new PdfFormatException("Object not recognized.", cbuf.position());
	}

        /**
           Reads an individual (partial) cross-reference table and
           trailer dictionary from the PDF document.  The trailer
           dictionary is filtered through
           <code>PdfReaderFilter</code>.  <b>This method should be
           made public.</b>
	   @param xrefTrailer an existing xrefTrailer object to add
	   data to; assumed to be the "subsequent" to the new
	   XrefTrailer that is to be read.  Only non-existing entries
	   are modified.  The trailer is not modified.
	   @param startxref the xref start position.
	   @param filter the filter.
	   @param prev the current Prev offset.
           @return the cross-reference table and trailer.
           @throws IOException
           @throws PdfFormatException
        */
        protected XrefTable readPartialXrefTable(XrefTable xt, long startxref,
						 long[] prev) throws IOException, PdfFormatException {

		Matcher m;

		// there is no way to determine how large a block to
		// read that will contain the entire xref section; so
		// we must try progressively larger blocks until we
		// can match the whole section
		ByteBuffer bbuf;
		CharBuffer cbuf;
		int blockSize;
		if (xt != null) {
			// we can use the xref table size as a hint
			blockSize = (xt.size() * 20) + 8192;
		} else {
			blockSize = 65536;
		}
		long inputLength = _pdfInput.getLength();
		long endtrailer;
		boolean done = false;
		do {
			endtrailer = startxref + blockSize;
			if ( endtrailer > inputLength ) {
				endtrailer = inputLength;
			}
			cbuf = _pdfInput.readChars(startxref, endtrailer);
			if (endtrailer == inputLength) {
				done = true;
				break;
			}
			m = _patXrefEof.matcher(cbuf);
			blockSize = blockSize * 2;
			if (m.lookingAt()) {
				done = true;
				break;
			}
		} while ( !done );
		
		XrefTable r;

		if (xt != null) {
			
			r = xt;
			
		} else {
			
			// first read past xref table to get trailer
			m = _patXrefTable.matcher(cbuf);
			if ( !(m.lookingAt()) ) {
				throw new PdfFormatException(
					"Cross-reference table or trailer not found at correct position.", startxref);
			}
			
			int trailer_offset = cbuf.position() + m.end();
			
			// read trailer
			PdfObject pobj = readObject(startxref + trailer_offset, endtrailer,
						    false, null);
			if ( !(pobj instanceof PdfDictionary) ) {
				throw new PdfFormatException(
					"Trailer dictionary not found.", trailer_offset);
			}
			
			// get Prev value
			PdfDictionary trailerDictionary = (PdfDictionary)pobj;
			Map trailerMap = trailerDictionary.getMap();
			Object obj = trailerMap.get(PDFNAME_PREV);
			if (obj == null) {
				prev[0] = -1;
			} else {
				if ( (!(obj instanceof PdfInteger)) &&
				     (!(obj instanceof PdfFloat)) ) {
					throw new PdfFormatException(
						"Valid Prev value not found in trailer dictionary.",
						trailer_offset);
				}
				prev[0] = ((PdfNumber)obj).getLong();
			}
			
			// get xref size
			obj = trailerMap.get(PDFNAME_SIZE);
			if ( !(obj instanceof PdfInteger) ) {
				throw new PdfFormatException(
					"Valid xref size not found in trailer dictionary.", trailer_offset);
			}
			int xrefSize = ((PdfInteger)obj).getInt();
			// initialize XrefTable
			long[] rindex = new long[xrefSize];
			int[] rgeneration = new int[xrefSize];
			byte[] rusage = new byte[xrefSize];
			r = XrefTable.wrap(rindex, rgeneration, rusage, trailerDictionary);
			
			// rewind to xref beginning
			cbuf.position(0);
		}

		// added startxref to XrefTable's startxref list
		r.getStartxrefList().add(new Long(startxref));
		
		m = _patXref.matcher(cbuf);
		if ( !(m.lookingAt()) ) {
			throw new PdfFormatException(
				"Cross-reference table (xref) not found at correct position.", 0);
		}
		cbuf.position(cbuf.position() + m.end());

		String s;
		done = false;
		do {
			m = _patXrefSub.matcher(cbuf);
			if ( !(m.lookingAt()) ) {
				throw new PdfFormatException(
					"Cross-reference table (subsection) not found.", 0);
			}
			s = m.group().trim();
			if (s.equals("trailer")) {
				done = true;
				break;
			}
			cbuf.position(cbuf.position() + m.end());
			String[] sp = s.split(" ");
			int x = Integer.parseInt(sp[0]);
			int n = Integer.parseInt(sp[1]);
			char[] ca = new char[11];
			
			long[] index = r.unwrapIndexArray();
			int[] generation = r.unwrapGenerationArray();
			byte[] usage = r.unwrapUsageArray();
			
			for ( ; n > 0; n--, x++) {
				
				// check for existing data
				if ( (xt != null) && (usage[x] != XrefTable.ENTRY_UNDEFINED) ) {
					cbuf.position(cbuf.position() + 20);
				} else {
					// add the data
					cbuf.get(ca, 0, 11);
					index[x] = Long.parseLong(new String(ca, 0, 10));
					cbuf.get(ca, 0, 6);
					generation[x] = Integer.parseInt(new String(ca, 0, 5));
					cbuf.get(ca, 0, 3);
					usage[x] = (ca[0] == 'n') ?
						XrefTable.ENTRY_IN_USE :
						XrefTable.ENTRY_FREE;
				}
			}
		} while (!done);
		
		// if this is not the most recently updated
		// xref, then we didn't need to read the
		// trailer beforehand (to get the table size);
		// so now we do it here.
		if (xt != null) {
			int trailer_offset = cbuf.position() + m.end();
			// read trailer
			PdfObject pobj = readObject(startxref + trailer_offset, endtrailer, false, null);
			if ( !(pobj instanceof PdfDictionary) ) {
				throw new PdfFormatException(
					"Trailer dictionary not found.", trailer_offset);
			}
			
			// get Prev value
			PdfDictionary trailerDictionary = (PdfDictionary)pobj;
			Map trailerMap = trailerDictionary.getMap();
			Object obj = trailerMap.get(PDFNAME_PREV);
			if (obj == null) {
				prev[0] = -1;
			} else {
				if ( (!(obj instanceof PdfInteger)) &&
				     (!(obj instanceof PdfLong)) ) {
					throw new PdfFormatException(
						"Valid Prev value not found in trailer dictionary.",
						trailer_offset);
				}
				prev[0] = ((PdfNumber)obj).getInt();
			}
		}
		
		return r;
        }

        /**
           Reads the header of the PDF document.
           @return the PDF document header.
           @throws IOException
           @throws PdfException
        */
        public String readHeader() throws IOException, PdfException {
		synchronized (this) {
			// searches within the first 1024 bytes for a header
			// in the form "%PDF-M.m" or "%!PS-Adobe-N.n PDF-M.m"
			// where N.n is an Adobe Document Structuring
			// Conventions version number and M.m is a PDF version
			// number.
			CharBuffer cbuf = _pdfInput.readChars(0, Math.min(1024, _pdfInput.getLength()));
			Matcher m = _patHeader.matcher(cbuf);
			if (m.find()) {
				return m.group().trim();
			}
			throw new PdfFormatException("PDF document header not found.", 0);
		}
        }

        /**
           Reads a PDF object from the document.  The object is
	   filtered through <code>PdfReaderFilter</code>.  It is
	   possible for this method to return <code>null</code> if the
	   filtering method discards all objects.
	   @param start the offset where the object starts.
	   @param end the offset where the object ends.
	   @param indirect true if the object is preceded by the object
	   number, generation, and "obj".
	   @param xt the PDF document's cross-reference table.
	   @param filter the object filter.
           @return the PDF object.
           @throws IOException
           @throws PdfFormatException
        */
        public PdfObject readObject(long start, long end, boolean indirect,
				    XrefTable xt) throws IOException, PdfFormatException {
		synchronized (this) {

			CharBuffer cbuf = _pdfInput.readChars(start, end);

			if (indirect) {
				// move past the introduction
				Matcher m = _patObjIntro.matcher(cbuf);
				if ( !(m.lookingAt()) ) {
					throw new PdfFormatException(
						"Object not found.", start);
				}
				cbuf.position(m.end() - 1);
			}
			
			return parseObject(start, end, cbuf, xt);
			
		}
        }

        /**
           Reads the startxref value from the PDF document.
           @return the startxref value.
	   @throws IOException
           @throws PdfFormatException
        */
        public long readStartxref() throws IOException, PdfFormatException {
		synchronized (this) {

			long bufLength = _pdfInput.getLength();
			CharBuffer cbuf = _pdfInput.readChars(
				Math.max(bufLength - (STARTXREF_RETRY_COUNT * STARTXREF_RETRY_SCAN), 0),
				bufLength);

			Matcher m = _patStartxref.matcher(cbuf);
			int start = cbuf.capacity();
			for (int retry = PdfReader.STARTXREF_RETRY_COUNT; retry > 0; retry--) {
				start -= PdfReader.STARTXREF_RETRY_SCAN;
				if (start >= 0) {
					if (m.find(start)) {
						String s = m.group().trim();
						String[] sp = s.split(PdfReader.REGEX_WHITESPACE);
						return Long.parseLong(sp[sp.length - 1]);
					}
				} else break;
			}
			throw new PdfFormatException("PDF startxref not found.", 0);
		}
        }

        /**
           Reads and compiles all cross-reference tables and trailer
           dictionaries from the PDF document beginning at a specified
           position.  The most recent trailer dictionary is filtered
           through <code>PdfReaderFilter</code>.
	   @param startxref the xref start position.
	   @param filter the filter.
           @return the cross-reference table and trailer.
           @throws IOException
           @throws PdfFormatException
        */
        public XrefTable readXrefTable(long startxref) throws IOException, PdfFormatException {
		synchronized (this) {
			XrefTable xt = null;
			long start = startxref;
			long[] prev = new long[1];
			do {
				xt = readPartialXrefTable(xt, start, prev);
				start = prev[0];
				
			} while (start != -1);
			xt.createSortedIndexArray();
			return xt;
		}
	}
	
}
