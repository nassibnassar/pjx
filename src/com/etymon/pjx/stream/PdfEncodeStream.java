/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Provides commonly used stream encoding functions.  This class is
   synchronized.
   @author Nassib Nassar
*/
public class PdfEncodeStream {

        protected static final PdfName PDFNAME_FILTER = new PdfName("Filter");
	protected static final PdfName PDFNAME_FLATEDECODE = new PdfName("FlateDecode");
	protected static final PdfName PDFNAME_LZWDECODE = new PdfName("LZWDecode");
	protected static final PdfName PDFNAME_RUNLENGTHDECODE = new PdfName("RunLengthDecode");
	protected static final PdfName PDFNAME_CCITTFAXDECODE = new PdfName("CCITTFaxDecode");
	protected static final PdfName PDFNAME_JBIG2DECODE = new PdfName("JBIG2Decode");
	protected static final PdfName PDFNAME_DCTDECODE = new PdfName("DCTDecode");

	/**
	   The set of stream filters used for compression.
	 */
	protected Set _compressionFilters;

	/**
	 */
	protected PdfFlateFilter _flateFilter;
	
	/**
	   The manager associated with the PDF document.
	*/
	protected PdfManager _m;

	/**
	   Constructs an instance of this class with a specified
	   manager.
	   @param manager the manager instance.
	 */
	public PdfEncodeStream(PdfManager manager) {
		_m = manager;
		_flateFilter = new PdfFlateFilter(manager);

		_compressionFilters = new HashSet(6);
		_compressionFilters.add(PDFNAME_FLATEDECODE);
		_compressionFilters.add(PDFNAME_LZWDECODE);
		_compressionFilters.add(PDFNAME_RUNLENGTHDECODE);
		_compressionFilters.add(PDFNAME_CCITTFAXDECODE);
		_compressionFilters.add(PDFNAME_JBIG2DECODE);
		_compressionFilters.add(PDFNAME_DCTDECODE);
	}
	
	/**
	   Encodes a stream using the Flate compression method (based
	   on zlib/deflate).  If the stream is already compressed with
	   the Flate, LZW, RunLength, CCITTFax, JBIG2, or DCT method,
	   then this method returns the original stream unmodified.
	   This method uses {@link PdfFlateFilter#encode(PdfStream)
	   PdfFlateFilter.encode(PdfStream)} to perform the encoding.
	   @param stream the stream to encode.
	   @return the encoded stream, or the original stream if it is
	   already compressed.
	   @throws IOException
	   @throws PdfFormatException
	 */
	public PdfStream compressFlate(PdfStream stream) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {
				
				PdfManager m = _m;

				// get the set of filters already used
				// on this stream, and if any of them
				// are compression filters, simply
				// return the original stream
				Object obj = stream.getDictionary().getMap().get(PDFNAME_FILTER);
				if (PdfNull.isNull(obj) == false) {
					if ( !(obj instanceof PdfObject) ) {
						throw new PdfFormatException("Filter name is not a PDF object.");
					}
					obj = m.getObjectIndirect((PdfObject)obj);
					if (PdfNull.isNull(obj) == false) {
						Set filters;
						if (obj instanceof PdfArray) {
							filters = new HashSet( ((PdfArray)obj).getList() );
						} else {
							filters = new HashSet();
							filters.add( obj );
						}
						filters.retainAll(_compressionFilters);
						if (filters.isEmpty() == false) {
							return stream;
						}
					}
				}

				// now compress the stream with Flate
				return _flateFilter.encode(stream);
				
			}
		}
	}
	
}
