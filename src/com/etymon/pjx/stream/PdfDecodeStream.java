/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import java.io.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Performs pipelined stream filtering to attempt to decode a stream.
   This class is synchronized.
   @author Nassib Nassar
*/
public class PdfDecodeStream {

        protected static final PdfName PDFNAME_FILTER = new PdfName("Filter");
	protected static final PdfName PDFNAME_FLATEDECODE = new PdfName("FlateDecode");
	
	/**
	   The manager associated with the PDF document.
	*/
	protected PdfManager _m;

	/**
	   A flate filter instance to use for decoding.
	 */
	protected PdfFlateFilter _flateFilter;
	
	/**
	   Constructs an instance of this class with a specified
	   manager.
	   @param manager the manager instance.
	 */
	public PdfDecodeStream(PdfManager manager) {
		_m = manager;
		_flateFilter = new PdfFlateFilter(manager);
	}

	/**
	   Applies a sequence of stream filter decoders to the
	   specified stream, based on the stream dictionary's Filter
	   entry, in order to decode the stream.  If the stream is
	   encoded with an unsupported filter, this method will throw
	   {@link PdfDecoderNotSupportedException
	   PdfDecoderNotSupportedException} to indicate that it is
	   unable to decode the stream.  If the stream is not encoded
	   with any filters, this method returns the original stream
	   unmodified.
	   @param stream the stream to decode.
	   @return the decoded stream, or the original stream if it is
	   not encoded with any filters.
	   @throws IOException
	   @throws PdfFormatException
	   @throws PdfDecoderException
	 */
	public PdfStream decode(PdfStream stream) throws IOException, PdfFormatException, PdfDecoderException {
		synchronized (this) {
			synchronized (_m) {
				
				PdfManager m = _m;
				
				// get the filter list
				List filters = getFilterList(m, stream.getDictionary().getMap());
				if (filters == null) {
					return stream;
				}
				
				// cycle through for each and decode via the
				// appropriate filter
				PdfStream filtered = stream;
				for (Iterator t = filters.iterator(); t.hasNext(); ) {
					
					// get the first filter
					Object obj = t.next();
					if ( !(obj instanceof PdfName) ) {
						throw new PdfFormatException("Stream filter is not a name object.");
					}
					PdfName filter = (PdfName)obj;
					
					if (filter.equals(_flateFilter.getName())) {
						filtered = _flateFilter.decode(filtered);
					} else {
						throw new PdfDecoderNotSupportedException(
							"Stream filter decoder \"" +
							filter.getString() + "\" not supported.");
					}
					
				}
				
				// return the resultant stream
				return filtered;

			}
		}
	}
	
	/**
	   Removes the first element of a filter list, adds the filter
	   list to a stream dictionary map, and returns the resultant
	   stream dictionary.
	   @param filters the filter list.
	   @param streamDict the stream dictionary map.
	   @throws PdfFormatException
	 */
	protected static void modifyFilterList(List filters, Map streamDict) throws PdfFormatException {

		// remove first element
		filters.remove(0);

		// add the filter list to the stream dictionary
		if (filters.size() == 0) {
			streamDict.remove(PDFNAME_FILTER);
		} else {
			streamDict.put(PDFNAME_FILTER, filters);
		}

	}

	/**
	   Extracts the filter list from a stream dictionary map.
	   @param manager the manager to use for indirect reference
	   look-ups.
	   @param streamDict the stream dictionary map.
	   @return the filter list.
	   @throws PdfFormatException
	 */
	protected static List getFilterList(PdfManager manager, Map streamDict) throws IOException, PdfFormatException {
		
		Object obj = streamDict.get(PDFNAME_FILTER);
		if (PdfNull.isNull(obj)) {
			return null;
		}
		if ( !(obj instanceof PdfObject) ) {
			throw new PdfFormatException("Filter name is not a PDF object.");
		}
		obj = manager.getObjectIndirect((PdfObject)obj);
		if (PdfNull.isNull(obj)) {
			return null;
		}
		if ( ( !(obj instanceof PdfName) ) &&
		     ( !(obj instanceof PdfArray) ) ) {
			throw new PdfFormatException("Filter name is not a name or array.");
		}
		List filters;
		if (obj instanceof PdfArray) {
			filters = new ArrayList( ((PdfArray)obj).getList() );
		} else {
			filters = new ArrayList();
			filters.add( (PdfName)obj );
		}
		return filters;

	}
	
}
