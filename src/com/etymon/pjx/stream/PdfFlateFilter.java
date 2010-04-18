/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.zip.*;
import com.etymon.pjx.*;

/**
   Implements a stream filter for Flate compression (based on
   zlib/deflate).  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfFlateFilter
	implements PdfStreamFilter {

        protected static final PdfName PDFNAME_FILTER = new PdfName("Filter");
        protected static final PdfName PDFNAME_FLATEDECODE = new PdfName("FlateDecode");

	/**
	   The manager associated with the PDF document.
	*/
	protected PdfManager _m;

	/**
	   A byte array used for buffering.
	*/
	protected byte[] _ba = new byte[16384];

	/**
	   Instance used for deflating.
	 */
	Deflater _deflater = new Deflater(9);
	
	/**
	   Instance used for inflating.
	 */
	Inflater _inflater = new Inflater();
	
	/**
	   Constructs an instance of this class with a specified
	   manager instance.
	   @param manager the manager instance.
	 */
	public PdfFlateFilter(PdfManager manager) {
		_m = manager;
	}

	public PdfName getName() {
		return PDFNAME_FLATEDECODE;
	}

	public PdfStream encode(PdfStream stream) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {
		
				PdfManager m = _m;
				
				// add /FlateDecode to the pipeline
				Map dict = new HashMap( stream.getDictionary().getMap() );
				List filters = PdfDecodeStream.getFilterList(m, dict);
				if (filters == null) {
					dict.put(PDFNAME_FILTER, PDFNAME_FLATEDECODE);
				} else {
					List newFilters = new ArrayList(filters.size() + 1);
					newFilters.add(PDFNAME_FLATEDECODE);
					newFilters.addAll(filters);
					dict.put(PDFNAME_FILTER, new PdfArray(newFilters));
				}
				
				// encode the stream
				Deflater deflater = _deflater;
				ByteBuffer bb = stream.getBuffer();
				byte[] bba = new byte[bb.capacity()];
				bb.get(bba);
				deflater.setInput(bba);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int len;
				byte[] ba = _ba;
				do {
					do {
						while ( (len = deflater.deflate(ba)) != 0) {
							baos.write(ba, 0, len);
						}
					} while (deflater.needsInput() == false);
					deflater.finish();
				} while (deflater.finished() == false);
				deflater.reset();
				baos.close();
				
				byte[] nbba = baos.toByteArray();
				ByteBuffer nbb = ByteBuffer.wrap(nbba);

				return new PdfStream( new PdfDictionary(dict),
						      nbb );
				
			}
		}
	}
	
	public PdfStream decode(PdfStream stream) throws IOException, PdfFormatException, PdfDecoderFormatException {
		synchronized (this) {
			synchronized (_m) {
		
				PdfManager m = _m;
				
				// check that /FlateDecode is first in the pipeline
				Map dict = new HashMap( stream.getDictionary().getMap() );
				List filters = PdfDecodeStream.getFilterList(m, dict);
				if ( (filters == null) ||
				     ( ((PdfName)filters.get(0)).equals(PDFNAME_FLATEDECODE) == false) ) {
					throw new PdfDecoderFormatException("Incorrect decoding method for this stream.");
				}
				// now we have confirmed that /FlateDecode is first
				
				// remove /FlateDecode
				PdfDecodeStream.modifyFilterList(filters, dict);
				
				// decode the stream
				Inflater inflater = _inflater;
				ByteBuffer bb = stream.getBuffer();
				byte[] bba = new byte[bb.capacity()];
				bb.get(bba);
				inflater.setInput(bba);
				if (inflater.needsDictionary()) {
					throw new PdfDecoderFormatException("Flate compression needs dictionary.");
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream(bba.length);
				int len;
				byte[] ba = _ba;
				try {
					while ( (len = inflater.inflate(ba)) != 0) {
						baos.write(ba, 0, len);
					}
				} catch (DataFormatException e) {
					throw new PdfDecoderFormatException( e.getMessage() );
				}
				if (inflater.finished() == false) {
					throw new PdfDecoderFormatException("Flate decoder: unexpected end of stream.");
				}
				inflater.reset();
				baos.close();
				
				byte[] nbba = baos.toByteArray();
				ByteBuffer nbb = ByteBuffer.wrap(nbba);

				return new PdfStream( new PdfDictionary(dict),
						      nbb );
				
			}
		}
	}
	
}
