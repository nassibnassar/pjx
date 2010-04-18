/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
   Represents the PDF stream object.  This class is synchronized.
   @author Nassib Nassar
*/
public class PdfStream
	extends PdfObject {

	/**
	   The byte sequence contained by the stream.  The position is
	   maintained at 0, and the limit is maintained at capacity().
	*/
	protected ByteBuffer _bb;

	/**
	   The stream dictionary.
	*/
	protected PdfDictionary _d;

	/**
           A <code>PdfName</code> object representing the name
           <code>Length</code>.
	*/
	protected static final PdfName PDFNAME_LENGTH = new PdfName("Length");

	/**
	   A protected constructor intended to be called only from
	   {@link #wrap(PdfDictionary, ByteBuffer) wrap(PdfDictionary,
	   ByteByffer)}.
	 */
	protected PdfStream() {
	}

	/**
	   Constructs a stream object from a PDF dictionary and a
	   <code>ByteBuffer</code>.  The stream's byte sequence is
	   read from the <code>ByteBuffer</code> starting at its
	   current position and ending at its limit.
	   @param d the PDF dictionary.
	   @param bb the <code>ByteBuffer</code>.
	 */
	public PdfStream(PdfDictionary d, ByteBuffer bb) {

		int streamLength = bb.remaining();
		
		Map map = new HashMap(d.getMap());
		map.put(PDFNAME_LENGTH, new PdfInteger(streamLength));
		_d = new PdfDictionary(map);
		
		_bb = ByteBuffer.allocateDirect(streamLength);
		_bb.put(bb);
		_bb.position(0);

	}

	/**
	   Returns the byte sequence contained in this stream.
	   @return the byte sequence.  The returned
	   <code>ByteBuffer</code> object is read-only.
	 */
	public ByteBuffer getBuffer() {
		synchronized (this) {
			ByteBuffer bb = _bb.asReadOnlyBuffer();
			bb.position(0);
			bb.limit(bb.capacity());
			return bb;
		}
	}

	/**
	   Returns this stream's dictionary.
	   @return the stream dictionary.
	 */
	public PdfDictionary getDictionary() {
		synchronized (this) {
			return _d;
		}
	}

	/**
	   A factory for fast construction of this class.  The
	   constructed object will be a wrapper around the specified
	   <code>PdfDictionary</code> and <code>ByteBuffer</code>.
	   The entire capacity of <code>ByteBuffer</code> is assumed
	   to represent the stream's byte sequence.  The Length value
	   in the stream dictionary is assumed to be correct.  The
	   calling method must ensure that the <code>ByteBuffer</code>
	   is never externally modified or accessed, in order to meet
	   the immutability requirement of {@link PdfObject
	   PdfObject}.
	   @param m the <code>PdfDictionary</code> and
	   <code>ByteBuffer</code> to be used to back this stream.
	   @return the constructed object.
	 */
	protected static PdfStream wrap(PdfDictionary d, ByteBuffer bb) {
		PdfStream ps = new PdfStream();
		ps._d = d;
		bb.position(0);
		bb.limit(bb.capacity());
		ps._bb = bb;
		return ps;
	}
	
	protected int writePdf(PdfWriter w, boolean spacing) throws IOException {
		synchronized (this) {

			DataOutputStream dos = w.getDataOutputStream();
			FileChannel fc = w.getFileChannel();
			
			int count = _d.writePdf(w, false);
			
			dos.writeBytes("\nstream\n");
			count += 8;

			ByteBuffer bb = _bb;
			int bbcap = bb.capacity();
			if (fc != null) {
				dos.flush();
				fc.write(bb);
			} else {
				byte[] b = new byte[bbcap];
				bb.get(b);
				dos.write(b);
			}
			count += bbcap;
			bb.position(0);
			
			dos.writeBytes("endstream");
			return count + 9;
			
		}
	}

	public boolean equals(Object obj) {
		synchronized (this) {

			if ( (obj == null) || ( !(obj instanceof PdfStream) ) ) {
				return false;
			}

			PdfStream s = (PdfStream)obj;

			return ( ( _d.equals(s._d) ) &&
				 ( _bb.equals(s._bb) ) );

		}
	}

	public int hashCode() {
		synchronized (this) {
			return _d.hashCode() ^ _bb.hashCode();
		}
	}

}
