/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

/**
   Provides low-level methods for reading portions of a PDF document.
   {@link PdfReader PdfReader} accesses PDF documents through the
   {@link PdfInput PdfInput} interface, which is implemented by this
   class.  The portions of the document are read from the file system
   as they are requested, which reduces memory consumption as compared
   to {@link PdfInputBuffer PdfInputBuffer} but is a bit slower.  <p>
   This class is synchronized; however, note that since it acts as a
   wrapper around a file that is kept open, it is the calling method's
   responsibility to ensure that the file is not modified externally
   to this class.  If that is a problem, use {@link
   PdfInputBuffer#PdfInputBuffer(File)
   PdfInputBuffer.PdfInputBuffer(File)}, which reads the entire file
   into memory and closes it immediately.
   @author Nassib Nassar
 */
public class PdfInputFile
	implements PdfInput {

	/**
	   The file channel associated with the PDF document.
	*/
	protected FileChannel _fileChannel;

	/**
	   The length of the input file.
	 */
	protected long _length;

	/**
	   The input file name.
	 */
	protected String _name;

	/**
	   The random access file containing the PDF document.
	 */
	protected RandomAccessFile _randomAccessFile;

	/**
	  Constructs a PDF input source based on a specified file.
	  The file is kept open, and portions of it are read as they
	  are requested.  It is the calling method's responsibility to
	  ensure that the file is not modified externally to this
	  class.
	  @param pdfFile the source file.
	  @throws IOException
	 */
	public PdfInputFile(File pdfFile) throws IOException {

		_randomAccessFile = new RandomAccessFile(pdfFile, "r");
		_fileChannel = _randomAccessFile.getChannel();
		_length = _randomAccessFile.length();
		_name = pdfFile.getPath();

	}
	
        /**
           Closes the PDF document and releases any system resources
           associated with it.
           @throws IOException
        */
        public void close() throws IOException {
		synchronized (this) {
			if (_fileChannel != null) {
				_fileChannel.close();
				_fileChannel = null;
			}
			if (_randomAccessFile != null) {
				_randomAccessFile.close();
				_randomAccessFile = null;
			}
		}
        }

	public long getLength() {
		synchronized (this) {
		
			return _length;

		}
	}

	public ByteBuffer readBytes(long start, long end) throws IOException {
		synchronized (this) {

			ByteBuffer bbuf = ByteBuffer.allocateDirect((int)(end - start));
			_fileChannel.read(bbuf, start);
			bbuf.position(0);
			return bbuf;
			
		}
	}

	public CharBuffer readChars(long start, long end) throws IOException {
		synchronized (this) {

			
			try {
				return Charset.forName("ISO-8859-1").newDecoder().decode(
					readBytes(start, end) );
				// Unicode is "UTF-16"
			}
			catch (CharacterCodingException e) {
				e.printStackTrace();
				Runtime.getRuntime().exit(-1);
			}

			return null;  // should never happen
			
		}
	}

	public String getName() {
		synchronized (this) {

			return _name;

		}
	}

}
