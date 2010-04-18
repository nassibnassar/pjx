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
   class.  The entire buffer is stored in memory (except in the case
   of {@link #PdfInputBuffer(File, boolean) PdfInputBuffer(File,
   true)}); having the buffer in memory generally improves processing
   speed as compared to {@link PdfInputFile PdfInputFile}, but of
   course it requires the memory to be available and therefore puts
   greater stress on system resources.  For large PDF documents, it is
   normally better to use {@link PdfInputFile PdfInputFile}
   instead. <p> This class is synchronized; however, note that since
   it acts as a wrapper around a buffer (in the case of {@link
   #PdfInputBuffer(ByteBuffer, String) PdfInputBuffer(ByteBuffer,
   String)}), it is the calling method's responsibility to ensure that
   the buffer is not modified externally to this class.  In the case
   of {@link #PdfInputBuffer(File) PdfInputBuffer(File)} or {@link
   #PdfInputBuffer(File, boolean) PdfInputBuffer(File, false)}, the
   constructor reads the entire file into a buffer and that buffer is
   not externally accessible; so there is no such danger.  However,
   {@link #PdfInputBuffer(File, boolean) PdfInputBuffer(File, true)}
   leaves the file open, and therefore the calling method must ensure
   that the file is not modified externally to this class.
   @author Nassib Nassar
 */
public class PdfInputBuffer
	implements PdfInput {

	/**
	   The byte buffer containing the PDF raw data.
	*/
	protected ByteBuffer _bbuf;
	
	/**
	   The char buffer associated with <code>_bbuf</code>.
	*/
	protected CharBuffer _cbuf;
	
	/**
	   The file channel associated with the PDF document.
	*/
	protected FileChannel _fileChannel;

	/**
	   The file input stream associated with the PDF document.
	*/
	protected FileInputStream _fileInputStream;

	/**
	   The file name or assigned name of this buffer.
	 */
	protected String _name;
	
	/**
	  Constructs a PDF input source based on a specified
	  <code>ByteBuffer</code>.  The buffer is read starting at its
	  current position until no more bytes are remaining.  It is
	  the calling method's responsibility to ensure that the
	  buffer is not modified externally to this class.  A name can
	  optionally be assigned to this document for identification.
	  @param pdfBuffer the source buffer.
	  @param name the name to assign to the document (or
	  <code>null</code> if no name is to be assigned).
	  @throws IOException
	 */
        public PdfInputBuffer(ByteBuffer pdfBuffer, String name) throws IOException {

		_bbuf = pdfBuffer.slice();

		_name = (name != null) ? name : new String();
		
		initBuffer();

        }

	/**
	  Constructs a PDF input source based on a specified file.
	  This is equivalent to {@link #PdfInputBuffer(File, boolean)
	  PdfInputBuffer(File, false)}; i.e. this constructor reads
	  the entire file into memory and closes the file, and the
	  file is not memory-mapped.  The size of the file must not be
	  greater than 2<sup>31</sup>-1; use {@link PdfInputFile
	  PdfInputFile} for huge files.
	  @param pdfFile the source file.
	  @throws IOException
	 */
	public PdfInputBuffer(File pdfFile) throws IOException {

		initFile(pdfFile, false);

	}

	/**
	  Constructs a PDF input source based on a specified file,
	  with optional memory-mapping.  If memory-mapping is
	  disabled, this constructor reads the entire file into memory
	  and closes the file; if it is enabled, the file is kept
	  open, and it is the calling method's responsibility to
	  ensure that the file is not modified externally to this
	  class.  The memory-mapping feature may be useful for very
	  large files, but in such cases {@link PdfInputFile
	  PdfInputFile} makes more sense in order to minimize stress
	  on system resources; therefore using the memory-mapping
	  feature is not generally recommended.  The size of the file
	  must not be greater than 2<sup>31</sup>-1; use {@link
	  PdfInputFile PdfInputFile} for huge files.
	  @param pdfFile the source file.
	  @param memoryMapped specifies whether the file is to be
	  memory-mapped.  A value of <code>true</code> enables
	  memory-mapping.
	  @throws IOException
	 */
	public PdfInputBuffer(File pdfFile, boolean memoryMapped) throws IOException {

		initFile(pdfFile, memoryMapped);

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
			if (_fileInputStream != null) {
				_fileInputStream.close();
				_fileInputStream = null;
			}
		}
        }

	/**
	   Performs initialization common to multiple constructors of
	   this class.  This method is only intended to be called from
	   the constructors.
	 */
	protected void init() {

		_bbuf.position(0);
		try {
			_cbuf = Charset.forName("ISO-8859-1").newDecoder().decode(_bbuf);
			// Unicode is "UTF-16"
		}
		catch (CharacterCodingException e) {
			e.printStackTrace();
			Runtime.getRuntime().exit(-1);
		}

	}
	
	/**
	   Performs initialization common to multiple constructors of
	   this class.  This method is only intended to be called from
	   the constructors.
	 */
	protected void initBuffer() {

		_fileInputStream = null;
		_fileChannel = null;

		init();
		
	}

	/**
	   Performs initialization common to multiple constructors of
	   this class.  This method is only intended to be called from
	   the constructors.
	 */
	protected void initFile(File pdfFile, boolean memoryMapped) throws IOException {

		_name = pdfFile.getPath();
		
		if (memoryMapped) {

			_fileInputStream = new FileInputStream(pdfFile);
			_fileChannel = _fileInputStream.getChannel();
			long fileChannelSize = _fileChannel.size();
			if (fileChannelSize > Integer.MAX_VALUE) {
				_fileChannel.close();
				_fileInputStream.close();
				throw new IllegalArgumentException(
					"Huge files not supported by this class; use PdfInputFile.");
			}
			_bbuf = _fileChannel.map(FileChannel.MapMode.READ_ONLY,
						 0, (int)fileChannelSize);
			init();
			
		} else {

			FileInputStream fis = new FileInputStream(pdfFile);
			FileChannel fc = fis.getChannel();
			long fcSize = fc.size();
			if (fcSize > Integer.MAX_VALUE) {
				fc.close();
				fis.close();
				throw new IllegalArgumentException(
					"Huge files not supported by this class; use PdfInputFile.");
			}
			_bbuf = ByteBuffer.allocateDirect((int)fcSize);
			fc.read(_bbuf);
			fc.close();
			fis.close();
			
			initBuffer();
			
		}
		
	}
	
	public ByteBuffer readBytes(long start, long end) throws IOException {
		synchronized (this) {

			_bbuf.limit((int)end);
			_bbuf.position((int)start);
			return _bbuf.slice();
			
		}
	}

	public CharBuffer readChars(long start, long end) throws IOException {
		synchronized (this) {

			_cbuf.limit((int)end);
			_cbuf.position((int)start);
			return _cbuf.slice();
			
		}
	}

	public long getLength() {
		synchronized (this) {
		
			return _bbuf.capacity();
			
		}
	}

	public String getName() {
		synchronized (this) {
		
			return _name;
			
		}
	}

}
