/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
   Writes a PDF document.  Most applications do not need to access
   methods in this class but should instead go through {@link
   PdfManager PdfManager}.  This class is synchronized.
   @author Nassib Nassar
*/
public final class PdfWriter {

	/**
	   The buffered output stream associated with the PDF document.
	*/
	protected BufferedOutputStream _bos;

	/**
	   The data output stream associated with the PDF document.
	*/
	protected DataOutputStream _dos;

	/**
	   The file channel associated with the PDF document.
	*/
	protected FileChannel _fc;

	/**
	   The file output stream associated with the PDF document.
	*/
	protected FileOutputStream _fos;

	/**
	   The header to write at the beginning of PDF files.
	*/
	protected static final String PDF_HEADER = "%PDF-" + PdfWriter.PDF_VERSION + "\n%\323\343\317\342\n";
	
	/**
	   The PDF version output by this package.
	*/
	protected static final String PDF_VERSION = "1.4";

        /**
           Creates a writer for a PDF document to be written to a
           file.  If the file exists, this constructor overwrites it.
           @param pdfFile the file to write the PDF document to.
           @throws IOException
        */
	public PdfWriter(File pdfFile) throws IOException {

		_fos = new FileOutputStream(pdfFile);
		_bos = new BufferedOutputStream(_fos);
		_dos = new DataOutputStream(_bos);
		_fc = _fos.getChannel();
	}
	
        /**
           Creates a writer for a PDF document to be written to an
           <code>OutputStream</code>.  The calling method should
           ensure that the specified stream is a buffered stream, if
           applicable.
           @param outputStream the stream to write the PDF document to.
           @throws IOException
        */
	public PdfWriter(OutputStream outputStream) throws IOException {

		_fos = null;
		_bos = null;
		_dos = new DataOutputStream(outputStream);
		_fc = null;
	}

	/**
           Closes the PDF document and releases any system resources
           associated with it.
           @throws IOException
        */
        public void close() throws IOException {
		synchronized (this) {
			if (_dos != null) {
				_dos.flush();
				if (_fc != null) {
					_fc.close();
					_fc = null;
				}
				if (_dos != null) {
					_dos.close();
					_dos = null;
				}
				if (_bos != null) {
					_bos.close();
					_bos = null;
				}
				if (_fos != null) {
					_fos.close();
					_fos = null;
				}
			}
		}
        }

	/**
	   Returns the data output stream associated with this writer.
	   @return the data output stream.
	 */
	protected DataOutputStream getDataOutputStream() {
		synchronized (this) {
			return _dos;
		}
	}
	
	/**
	   Returns the file channel associated with this writer.
	   @return the file channel.
	 */
	protected FileChannel getFileChannel() {
		synchronized (this) {
			return _fc;
		}
	}

	/**
	   Writes the contents of a <code>ByteBuffer</code> to the PDF
	   document.  The entire capacity of the buffer is written,
	   disregarding its position and limit.
	   @return the number of bytes written.
	   @deprecated It needs to be considered whether this method
	   should write the entire capacity of the buffer, if it is to
	   be made a public method.
	 */
	protected int writeByteBuffer(ByteBuffer byteBuffer) throws IOException {
		byteBuffer.position(0);
		byteBuffer.limit(byteBuffer.capacity());
		if (_fc != null) {
			_dos.flush();
			_fc.write(byteBuffer);
		} else {
			byte[] b = new byte[byteBuffer.capacity()];
			byteBuffer.get(b);
			_dos.write(b);
		}
		return byteBuffer.capacity();
	}

	/**
	   Writes an entire PDF document stream to the output PDF
	   document.  This is used for incremental update.
	   @param reader the source of the PDF document stream.
	   @return the number of bytes written.
	   @throws IOException
	 */
	public long writeCopy(PdfReader reader) throws IOException {
		synchronized (this) {
			synchronized (reader) {

				PdfInput pdfInput = reader.getPdfInput();
				long inputLen = pdfInput.getLength();
				
				if (inputLen <= Integer.MAX_VALUE) {

					return writeByteBuffer( pdfInput.readBytes(0, inputLen) );

				} else {

					// huge files have to be
					// written in blocks, since
					// Java buffers use int
					// instead of long; we should
					// probably do something like
					// this for normal files as
					// well, to use less memory
					int x = 0;
					long count = 0;
					while (x < inputLen) {

						count += writeByteBuffer(
							pdfInput.readBytes(x, Math.min(x + 65536,
										       inputLen) ) );
						x += 65536;

					}
					return count;
					
				}
				
			}
		}
	}

	/**
	   Writes a PDF header to the document.
	   @return the number of bytes written.
	   @throws IOException
	 */
	public int writeHeader() throws IOException {
		synchronized (this) {
			String header = PdfWriter.PDF_HEADER;
			_dos.writeBytes(header);
			return header.length();
		}
	}
	
	/**
	   Writes a PDF object to the document as an indirect object.
	   @param obj the object to write.
	   @param number the object number.
	   @param generation the generation number.
	   @return the number of bytes written.
	   @throws IOException
	 */
	public int writeObject(PdfObject obj, int number, int generation) throws IOException {
		synchronized (this) {

			return obj.writePdf(this, false);

		}
	}

	/**
	   Writes a PDF object to the document as an indirect object.
	   @param obj the object to write.
	   @param number the object number.
	   @param generation the generation number.
	   @return the number of bytes written.
	   @throws IOException
	 */
	public int writeObjectIndirect(PdfObject obj, int number, int generation) throws IOException {
		synchronized (this) {

			String s, t;
			int count = 0;
			
			if (number != -1) {
				s = Integer.toString(number) + " " +
					Integer.toString(generation) + " obj\n";
				_dos.writeBytes(s);
				count += s.length();
			}

			count += writeObject(obj, number, generation);

			if (number != -1) {
				_dos.writeBytes("\nendobj\n");
				count += 8;
			}

			return count;

		}
	}

	/**
	   Writes a cross-reference table (and associated trailer) to
	   the document.
	   @param xt the cross-reference table.
	   @param startxref the file offset of the beginning of the
	   cross-reference table.
	   @return the number of bytes written.
	   @throws IOException
	 */
	public int writeXrefTable(XrefTable xt, long startxref) throws IOException {
		synchronized (this) {
			return xt.writePdf(this, startxref);
		}
	}
	
}
