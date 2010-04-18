package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;

/**
   A representation of the PDF stream type.
   @author Nassib Nassar
*/
public class PjStream
	extends PjObject {

	/**
	   Creates a stream as a wrapper around a byte array.
	   @param s the byte array to use for this stream.
	*/
	public PjStream(byte[] s) {
		_d = new PjStreamDictionary();
		_s = s;
	}

	/**
	   Creates a stream as a wrapper around a PjStreamDictionary and
	   byte array.
	   @param d the dictionary to use for this stream.
	   @param s the byte array to use for this stream.
	*/
	public PjStream(PjStreamDictionary d, byte[] s) {
		_d = d;
		_s = s;
	}

	/**
	   Returns the PjStreamDictionary used in the representation of this
	   stream.
   	   @return the PjStreamDictionary used in the representation of this
   	   stream.
	*/
	public PjStreamDictionary getStreamDictionary() {
		return _d;
	}
	
	/**
	   Returns the byte array used in the representation of this
	   stream.
   	   @return the byte array used in the representation of this
   	   stream.
	*/
	public byte[] getBuffer() {
		return _s;
	}

	/**
	   Decompresses this stream if it is compressed with the Flate
	   algorithm.
	   @return a cloned, uncompressed version of this stream; or
	   this stream if it is not marked as being compressed with
	   Flate.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public PjStream flateDecompress() throws InvalidPdfObjectException {
		// first check if the FlateDecode filter is turned on;
		// if not, return this (we don't need to decompress).
		// if so, turn off the filter in the new dictionary
		Hashtable ht = _d.getHashtable();
		Object obj = ht.get(PjName.FILTER);
		if (obj == null) {
			return this;
		}
		if ( (obj instanceof PjName) || (obj instanceof PjArray) ) {
			PjStreamDictionary newPjd = null;
			Hashtable newHt;
			if (obj instanceof PjName) {
				PjName pjn = (PjName)obj;
				if ( ! pjn.equals(PjName.FLATEDECODE) ) {
					return this;
				} else {
					// remove the element from the cloned dictionary
					try {
						newPjd = (PjStreamDictionary)(_d.clone());
					}
					catch (CloneNotSupportedException e) {
						throw new InvalidPdfObjectException(e.getMessage());
					}
					newHt = newPjd.getHashtable();
					newHt.remove(PjName.FILTER);
				}
			}
			else if (obj instanceof PjArray) {
				PjArray pja = (PjArray)obj;
				Vector v = pja.getVector();
				int x;
				if ( (x = v.indexOf(PjName.FLATEDECODE)) == -1) {
					return this;
				} else {
					// remove the element from the cloned dictionary
					try {
						newPjd = (PjStreamDictionary)(_d.clone());
					}
					catch (CloneNotSupportedException e) {
						throw new InvalidPdfObjectException(e.getMessage());
					}
					newHt = newPjd.getHashtable();
					pja = (PjArray)(newHt.get(PjName.FILTER));
					v = pja.getVector();
					v.removeElementAt(x);
				}
			}
			// do the decompression
			InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(_s));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int length;
			byte[] buffer = new byte[PjConst.FLATE_BUFFER_SIZE];
			try {
				while ((length = in.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, length);
				}
				out.close();
				in.close();
			}
			catch (IOException e) {
				// not sure what would cause this exception in this case
				return this;
			}
			return new PjStream(newPjd, out.toByteArray());
		} else {
			throw new InvalidPdfObjectException("Stream filter is not a name or array.");
		}
	}

	/**
	   Compress this stream with the Flate algorithm if it is not
	   already compressed.
	   @return a cloned, compressed version of this stream; or
	   this stream if it is already compressed.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public PjStream flateCompress() throws InvalidPdfObjectException {
		// first check if any compression filters are turned on;
		// if so, return this (we don't need to compress).
		// if not, turn on the FlateDecode filter in the new dictionary
		Hashtable ht = _d.getHashtable();
		Object filter = ht.get(PjName.FILTER);
		if (filter != null) {
			if ( ( ! (filter instanceof PjName) ) && ( ! (filter instanceof PjArray) ) ) {
				throw new InvalidPdfObjectException("Stream filter is not a name or array.");
			}
			// get or create a vector with the list of filters
			Vector v = null;
			if (filter instanceof PjName) {
				v = new Vector();
				v.addElement(filter);
			}
			else if (filter instanceof PjArray) {
				v = ((PjArray)filter).getVector();
			}
			// see if any of the filters are compression filters
			PjName name;
			Enumeration m = v.elements();
			Object obj;
			while (m.hasMoreElements()) {
				obj = m.nextElement();
				if ( ! (obj instanceof PjName) ) {
					throw new InvalidPdfObjectException(
						"Stream filter array contins an object that is not a name.");
				}
				name = (PjName)obj;
				if ( (name.equals(PjName.LZWDECODE)) ||
				     (name.equals(PjName.RUNLENGTHDECODE)) ||
				     (name.equals(PjName.CCITTFAXDECODE)) ||
				     (name.equals(PjName.DCTDECODE)) ||
				     (name.equals(PjName.FLATEDECODE)) ) {
					return this;
				}
			}
		}
		// ok, clone the dictionary and add the FlateDecode filter
		PjStreamDictionary newPjd;
		try {
			newPjd = (PjStreamDictionary)(_d.clone());
		}
		catch (CloneNotSupportedException e) {
			throw new InvalidPdfObjectException(e.getMessage());
		}
		Hashtable newHt = newPjd.getHashtable();
		if (filter == null) {
			newHt.put(PjName.FILTER, PjName.FLATEDECODE);
		} else {
			if (filter instanceof PjArray) {
				Vector v = ((PjArray)filter).getVector();
				v.addElement(PjName.FLATEDECODE);
			} else {
				// filter must be a name, so make it into an array
				Vector v = new Vector();
				v.addElement(filter);
				v.addElement(PjName.FLATEDECODE);
				newHt.put(PjName.FILTER, new PjArray(v));
			}
		}
		// do the compression
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		DeflaterOutputStream out = new DeflaterOutputStream(byteArrayOut);
		ByteArrayInputStream in = new ByteArrayInputStream(_s);
		int length;
		byte[] buffer = new byte[PjConst.FLATE_BUFFER_SIZE];
		try {
			while ((length = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, length);
			}
			out.close();
			in.close();
		}
		catch (IOException e) {
			// not sure what would cause this exception in this case
			return this;
		}
		return new PjStream(newPjd, byteArrayOut.toByteArray());
	}
	
	/**
	   Sets the Length field in the stream dictionary to
	   accurately reflect the length of the stream.  It is not
	   normally necessary to call this method, because it gets
	   called implicitly by methods that output the object in PDF
	   format.
	*/
	public void setLength() {
		_d.getHashtable().put(new PjName("Length"),
				      new PjNumber(_s.length));
	}

	/**
	   Writes this PDF stream to a stream in PDF format.
	   @param os the stream to write to.
	   @return the number of bytes written.
	   @exception IOException if an I/O error occurs.
	*/
	public long writePdf(OutputStream os) throws IOException {
		setLength();
		long z = _d.writePdf(os);
		z = z + writeln(os, "");
		z = z + write(os, "stream\n");
		z = z + write(os, _s);
		z = z + write(os, "endstream");
		return z;
	}

	/**
	   Returns a deep copy of this object.
	   @return a deep copy of this object.
	   @exception CloneNotSupportedException if the instance can not be cloned.
	*/
	public Object clone() throws CloneNotSupportedException {
		return new PjStream((PjStreamDictionary)(_d.clone()), (byte[])(_s.clone()));
	}
	
	/**
	   Renumbers object references within this object.  This
	   method calls itself recursively to comprehensively renumber
	   all objects contained within this object.
	   @param map the table of object number mappings.  Each
	   object number is looked up by key in the hash table, and
	   the associated value is assigned as the new object number.
	   The map hash table should consist of PjNumber keys and
	   PjReference values.
	*/
	public void renumber(Hashtable map) {
		_d.renumber(map);
	}

	/**
	   Decode this stream if it is compressed with the Ascii85
	   algorithm.
	   @return a cloned, unencoded version of this stream; or
	   this stream if it is not marked as being compressed with
	   Ascii85.
	   @exception InvalidPdfObjectException if an invalid object
	   type is encountered.
	*/
	public PjStream ascii85Decode() throws InvalidPdfObjectException {
		// first check if the Ascii85Decode filter is turned on;
		// if not, return this (we don't need to decompress).
		// if so, turn off the filter in the new dictionary
		Hashtable ht = _d.getHashtable();
		Object obj = ht.get(PjName.FILTER);
		if (obj == null) {
			return this;
		}
		if ( (obj instanceof PjName) || (obj instanceof PjArray) ) {
			PjStreamDictionary newPjd = null;
			Hashtable newHt;
			if (obj instanceof PjName) {
				PjName pjn = (PjName)obj;
				if ( ! pjn.equals(PjName.ASCII85DECODE) ) {
					return this;
				} else {
					// remove the element from the cloned dictionary
					try {
						newPjd = (PjStreamDictionary)(_d.clone());
					}
					catch (CloneNotSupportedException e) {
						throw new InvalidPdfObjectException(e.getMessage());
					}
					newHt = newPjd.getHashtable();
					newHt.remove(PjName.FILTER);
				}
			}
			else if (obj instanceof PjArray) {
				PjArray pja = (PjArray)obj;
				Vector v = pja.getVector();
				int x;
				if ( (x=v.indexOf(PjName.ASCII85DECODE))==-1) {
					return this;
				} else {
					// remove the element from the cloned dictionary
					try {
						newPjd = (PjStreamDictionary)(_d.clone());
					}
					catch (CloneNotSupportedException e) {
						throw new InvalidPdfObjectException(e.getMessage());
					}
					newHt = newPjd.getHashtable();
					pja = (PjArray)(newHt.get(PjName.FILTER));
					v = pja.getVector();
					v.removeElementAt(x);
				}
			}
			// do the decoding
			// the raw data resides in _s.
			byte [] decodedBuf = ascii85Decode(_s);
			if(decodedBuf == null)
				return this;
			return new PjStream(newPjd, decodedBuf);
		} else {
			throw new InvalidPdfObjectException("Stream filter is not a name or array.");
		}
	}

	protected PjStreamDictionary _d;
	protected byte[] _s;
	
	static long bytesToLong(byte [] b, int offset, int len) {
		long val = 0, exp = 0;
		for(int i=offset+len-1;i >= offset;i--) {
			for(int j=7;j >= 0;j--) {
				byte mask = (byte)(128 >> j);
				if((b[i] & mask) == mask) {
					val += Math.pow(2, exp);
				}
				exp++;
			}
		}
		return val;
	}

	static char [] ascii85EncodeWord(long word) {
		long v1, v2, v3, v4, v5;
		long p4 = (long)Math.pow(85,4);
		long p3 = (long)Math.pow(85,3);
		long p2 = (long)Math.pow(85,2);
		v1 = word/p4;
		word = word - (v1*p4);
		v2 = word/p3;
		word = word - (v2*p3);
		v3 = word/p2;
		word = word - (v3*p2);
		v4 = word/85;
		word = word - (v4*85);
		v5 = word;
		char [] c = new char[5];
		c[0] = (char)(v1 + '!');
		c[1] = (char)(v2 + '!');
		c[2] = (char)(v3 + '!');
		c[3] = (char)(v4 + '!');
		c[4] = (char)(v5 + '!');

		return c;
	}

	public static byte[] ascii85Encode(byte [] src) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int groupCount = src.length/4;
			int extraCount = src.length%4;
			int i;
			for(i=0;i<groupCount;i++) {
				long word = bytesToLong(src, i*4, 4);
				if(word == 0) {
					out.write('z');
					continue;
				}
				char [] c = ascii85EncodeWord(word);
				out.write(c[0]);
				out.write(c[1]);
				out.write(c[2]);
				out.write(c[3]);
				out.write(c[4]);
			}
			if(extraCount > 0) {
				byte [] buf = new byte[4];
				if(extraCount == 1) {
					buf[3] = src[i*4];
					buf[2] = 0;
					buf[1] = 0;
					buf[0] = 0;
				} else if(extraCount ==2 ) {
					buf[3] = src[i*4+1];
					buf[2] = src[i*4];
					buf[1] = 0;
					buf[0] = 0;
				} else {
					buf[3] = src[i*4+2];
					buf[2] = src[i*4+1];
					buf[1] = src[i*4];
					buf[0] = 0;
				}
				long word = bytesToLong(buf, 0, 4);
				char [] c = ascii85EncodeWord(word);
				for(i=5-(extraCount+1);i<5;i++) {
					out.write(c[i]);
				}
			}
			out.write('~');
			out.write('>');
			out.close();
		} catch(IOException e) {
			System.out.println(e);
		}
		return out.toByteArray();	
	}

	static long toWord(byte [] b, int offset, int sigDigits) { 
		long v1, v2, v3, v4, v5;
		long p4 = (long)Math.pow(85,4);
		long p3 = (long)Math.pow(85,3);
		long p2 = (long)Math.pow(85,2);
		v1 = (b[offset]-'!') * p4;
		v2 = (b[offset+1]-'!') * p3;
		v3 = (b[offset+2]-'!') * p2;
		v4 = (b[offset+3]-'!') * 85;
		v5 = (b[offset+4]-'!');
		if(sigDigits == 5)
			return v1 + v2 + v3 + v4 + v5;
		else if (sigDigits == 4)
			return v2 + v3 + v4 + v5;
		else if (sigDigits == 3)
			return v3 + v4 + v5;
		else if (sigDigits == 2)
			return v4 + v5;
		else return v5;
	}

	public static byte[] ascii85Decode(byte [] src) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int ptr = 0;
			int len = src.length - 2;	// ignore end of 
							// data marker

			for(;;) {
				if(((len - ptr) < 5)&&(src[ptr] != 'z')) {
					break;
				}
				long word = 0;
				if(src[ptr] == 'z') {
					ptr++;
				} else {
					word = toWord(src, ptr, 5);
					ptr+=5;
				}
				byte b4 = (byte)(word & 255);
				byte b3 = (byte)((word>>>8) & 255);
				byte b2 = (byte)((word>>>16) & 255);
				byte b1 = (byte)((word>>>24) & 255);
				out.write(b1);
				out.write(b2);
				out.write(b3);
				out.write(b4);
			}
			if((len-ptr) > 0) {
				// We have extra bytes
				int count = len-ptr;
				// The acutal number of binary bytes is 1 less
				// than count
				byte [] buf = new byte[5];

				int pad = 5-count;
				int j = 0;
				for(int i=0;i<5;i++) {
					if(i<pad) {
						buf[j++] = 0;
					} else {
						buf[j++] = src[ptr];
						ptr++;
					}
				}
				long word = toWord(buf, 0, count);
				byte b4 = (byte)(word & 255);
				byte b3 = (byte)((word>>>8) & 255);
				byte b2 = (byte)((word>>>16) & 255);
				byte b1 = (byte)((word>>>24) & 255);
				count -= 1;
				if(count == 1) {
					out.write(b4);
				} else if(count == 2) {
					out.write(b3);
					out.write(b4);
				} else if(count == 3) {
					out.write(b2);
					out.write(b3);
					out.write(b4);
				}
			}
			out.close();
		} catch(IOException e) {
			System.out.println(e);
		}
		return out.toByteArray();	
	}
}
