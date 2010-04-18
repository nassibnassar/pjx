/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx.stream;

import java.io.*;
import com.etymon.pjx.*;

/**
   A filtering function that implements PDF stream filter encoding and
   decoding.
   @author Nassib Nassar
*/
public interface PdfStreamFilter {

	/**
	   Returns the name of this filter method.  This is the name
	   to be used in the stream dictionary.
	 */
	public PdfName getName();
	
	/**
	   Encodes a stream using this filter's encoding method.
	   @param stream the stream to encode.
	   @return the encoded stream.
	   @throws PdfFormatException
	 */
	public PdfStream encode(PdfStream stream) throws IOException, PdfFormatException;

	/**
	   Decodes a stream using this filter's decoding method.
	   @param stream the stream to decode.
	   @return the decoded stream.
	   @throws PdfFormatException
	 */
	public PdfStream decode(PdfStream stream) throws IOException, PdfFormatException, PdfDecoderFormatException;

}
