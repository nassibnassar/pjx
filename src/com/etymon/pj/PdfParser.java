package com.etymon.pj;

import java.io.*;
import java.util.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;

public class PdfParser {

	public static void getObjects(Pdf pdf, RandomAccessFile raf)
		throws IOException, PjException {
		long[][] xref = getXref(pdf, raf);
		byte[] data;
		PjObject object;
		Hashtable ht = new Hashtable();
		for (int x = 0; x < xref.length; x++) {
			if (xref[x][2] == 1) {
				raf.seek(xref[x][0]);
				data = readUntil(raf, "endobj");
				object = PdfParser.parse(pdf, raf, xref, data, 0);
				pdf._objects.setObjectAt(object, x);
			}
		}
	}

	private static PjObject getObject(Pdf pdf, RandomAccessFile raf, long[][] xref, int num)
		throws IOException, PjException {
		// check if the object has been loaded
		PjObject obj = pdf._objects.objectAt(num);
		if (obj != null) {
			return obj;
		}
		// otherwise we have to load it
		raf.seek(xref[num][0]);
		byte[] data = readUntil(raf, "endobj");
		obj = PdfParser.parse(pdf, raf, xref, data, 0);
		pdf._objects.setObjectAt(obj, num);
		return obj;
	}
	
	private static long[][] getXref(Pdf pdf, RandomAccessFile raf) throws
		IOException, PjException {
		// we assume that the cross-reference table as a whole
		// (including all "sections") is contiguous in terms
		// of object numbers; in other words, we assume that
		// '/Size n' in the trailer dictionary indicates not
		// only that n is the number of cross reference
		// entries in the table, but also that (n-1) is the
		// largest object number in use; this allow us to use
		// a long[][] for storing the table, because we can
		// allocate it as long[n][3].  I think this is
		// implicit in the PDF spec but I couldn't find a
		// clear statement about it.  If it turns out that
		// this is incorrect, we'll have to change all the
		// code to use a Vector instead of an array.
		long lastXref = getStartXref(raf);
		return getNextXref(pdf, raf, lastXref, null);
	}

	private static long[][] getNextXref(Pdf pdf, RandomAccessFile raf, long start,
				   long[][] xref)
		throws IOException, PjException {
		raf.seek(start);
		byte[] xrefData = readUntil(raf, "trailer");
		byte[] trailerData = readUntil(raf, "startxref");
		PjDictionary trailer =
			(PjDictionary)(PdfParser.parse(pdf, raf, xref, trailerData, 0));
		Hashtable h = trailer.getHashtable();
		long[][] xr;
		if (xref == null) {
			xr = new long[((PjNumber)(h.get(
				new PjName("Size")))).getInt()][3];
			pdf._trailer = h;
		} else {
			xr = xref;
		}
		// recursively collect previous xref data
		PjNumber prev = (PjNumber)(h.get(new PjName("Prev")));
		if (prev != null) {
			xr = getNextXref(pdf, raf, prev.getLong(), xr);
		}
		// now overlay this xref data
		PdfParser.parseXref(xrefData, xr, 0);
		return xr;
	}

	private static long getStartXref(RandomAccessFile raf) throws
		IOException, PjException {
		// locate startxref near the end of the file
		int scan = 0;
		for (int retry = PjConst.SCAN_STARTXREF_RETRY; retry > 0; retry--) {
			scan = scan + PjConst.SCAN_STARTXREF;
			long fileSize = raf.length();
			raf.seek(fileSize - scan);
			byte[] buffer = readUntil(raf, "startxref");
			// next line should be the startxref value
			buffer = readUntil(raf, "%%EOF");
			if (buffer.length != 0) {
				// now parse the long value from the buffer
				StringBuffer sb = new StringBuffer();
				boolean abort = false;
				int x = 0;
				while ( (abort == false) && (Character.isDigit((char)(buffer[x]))) ) {
					sb.append((char)(buffer[x]));
					x++;
					if (x >= buffer.length) {
						abort = true;
					}
				}
				if (abort == false) {
					return new Long(new String(sb)).longValue();
				}
			}
		}
		throw new StartxrefFormatException("Unexpected end of file (startxref).");
	}

	public static byte[] readUntil(RandomAccessFile raf, String
					  endstr) throws IOException {
		StringBuffer sb = new StringBuffer();
		char c = '\0';
		String s;
		char[] compare = new char[endstr.length()];
		char lastEol = '\0';
		boolean eof = false;
		boolean done = false;
		do {
			try {
				c = (char)(raf.readUnsignedByte());
				switch (lastEol) {
				case '\0':
					if ( (c == '\r') || (c ==
							     '\n') ) {
						if (sb.length() >=
						    endstr.length()) {
							sb.getChars(sb.length() -
								    endstr.length(),
								    sb.length(),
								    compare, 0);
							s = new String(compare);
							if (s.equals(endstr)) {
								lastEol = c;
							}
						}
					}
					sb.append(c);
					break;
				case '\n':
					raf.seek(raf.getFilePointer() - 1);
					done = true;
					break;
				case '\r':
					if (c == '\n') {
						sb.append(c);
					} else {
						raf.seek(raf.getFilePointer()
							 - 1);
					}
					done = true;
					break;
				}
			}
			catch (EOFException e) {
				eof = true;
			}
		} while ( (eof == false) && (done == false) );
		int y = sb.length();
		byte[] buffer = new byte[y];
		for (int x = 0; x < y; x++) {
			buffer[x] = (byte)(sb.charAt(x));
		}
		return buffer;
	}
	
	// deprecated
	// RandomAccessFile.readLine() does not seem to work!
	// this is a replacement, but it also discards the trailing
	// '\r' and/or '\n'
	protected static String readLine(RandomAccessFile raf) throws
		IOException {
		char c = '\0';
		StringBuffer sb = new StringBuffer();
		boolean endOfLine = false;
		boolean endOfFile = false;
		boolean startOfNext = false;
		boolean firstChar = true;
		do {
			try {
				c = (char)(raf.readUnsignedByte());
				if ( (c != '\r') && (c != '\n') ) {
					if (endOfLine) {
						startOfNext = true;
					} else {
						sb.append(c);
					}
				} else {
					endOfLine = true;
				}
				firstChar = false;
			}
			catch (EOFException e) {
				endOfFile = true;
			}
		} while ( (endOfFile == false) && (startOfNext ==
						     false) );
		if (startOfNext) {
			raf.seek(raf.getFilePointer() - 1);
		}
		if ( (endOfFile) && (firstChar) ) {
			return null;
		} else {
			return sb.toString();
		}
	}

	
	public static void parseXref(byte[] data, long[][] xref, int start) throws XrefFormatException {
		PdfParserState state = new PdfParserState();
		state._data = data;
		state._pos = start;
		getLine(state);  // initial "xref"
		if (state._token.equals("xref") == false) {
			throw new XrefFormatException("Start of xref not found (xref).");
		}
		StringTokenizer st;
		int index, count, x;
		while (state._pos < state._data.length) {
			getLine(state);
			st = new StringTokenizer(state._token);
			if (state._token.equals("trailer")) {
				return;
			}
			index = Integer.parseInt(st.nextToken());
			count = Integer.parseInt(st.nextToken());
			for (x = 0; x < count; x++) {
				getLine(state);
				st = new StringTokenizer(state._token);
				xref[index][0] = new Integer(
					st.nextToken()).longValue();
				xref[index][1] = new Integer(
					st.nextToken()).longValue();
				if (st.nextToken().equals("n")) {
					xref[index][2] = 1;
				} else {
					xref[index][2] = 0;
				}
				index++;
			}
		}
	}
	
	public static PjObject parse(Pdf pdf, RandomAccessFile raf, long[][] xref, byte[] data, int start)
		throws IOException, PjException {
		PdfParserState state = new PdfParserState();
		state._data = data;
		state._pos = start;
		state._stream = -1;
		Stack stack = new Stack();
		boolean endFlag = false;
		while ( ( ! endFlag ) && (getToken(state)) ) {
			if (state._stream != -1) {
				stack.push(state._streamToken);
				state._stream = -1;
			}
			else if (state._token.equals("startxref")) {
				endFlag = true;
			}
			else if (state._token.equals("endobj")) {
				endFlag = true;
			}
			else if (state._token.equals("%%EOF")) {
				endFlag = true;
			}
			else if (state._token.equals("endstream")) {
				byte[] stream = (byte[])(stack.pop());
				PjStreamDictionary pjsd = new PjStreamDictionary(
					((PjDictionary)(stack.pop())).getHashtable());
				PjStream pjs = new PjStream(pjsd, stream);
				stack.push(pjs);
			}
			else if (state._token.equals("stream")) {
				// get length of stream
				PjObject obj = ((PjObject)(
					(((PjDictionary)(stack.peek())).
					getHashtable().
						    get(new PjName("Length")))));
				if (obj instanceof PjReference) {
					obj = getObject(pdf, raf, xref,
							((PjReference)(obj)).getObjNumber().getInt());
				}
				state._stream =
					((PjNumber)(obj)).getInt();

				// the following if() clause added to
				// handle the case of "Length" being
				// incorrect (larger than the actual
				// stream length)
				if ( state._stream >
				     (state._data.length - state._pos)
					) {
					state._stream =
						state._data.length -
						state._pos - 17;
				}

				if (state._pos < state._data.length) {
					if ((char)(state._data[state._pos]) == '\r') {
						state._pos++;
					}
					if ( (state._pos < state._data.length) &&
					     ((char)(state._data[state._pos]) ==
					      '\n') ) {
						state._pos++;
					}
				}
			}
			else if (state._token.equals("null")) {
				stack.push(new PjNull());
			}
			else if (state._token.equals("true")) {
				stack.push(new PjBoolean(true));
			}
			else if (state._token.equals("false")) {
				stack.push(new PjBoolean(false));
			}
			else if (state._token.equals("R")) {
				// we ignore the generation number
				// because all objects get reset to
				// generation 0 when we collapse the
				// incremental updates
				stack.pop();  // the generation number
				PjNumber obj = (PjNumber)(stack.pop());
				stack.push(new PjReference(obj, PjNumber.ZERO));
			}
			else if ( (state._token.charAt(0) == '<') &&
				  (state._token.startsWith("<<") == false) ) {
				stack.push(new PjString(PjString.decodePdf(state._token)));
			}
			else if (
				(Character.isDigit(state._token.charAt(0)))
				|| (state._token.charAt(0) == '-')
				|| (state._token.charAt(0) == '.') ) {
				stack.push(new PjNumber(new Float(state._token).floatValue()));
			}
			else if (state._token.charAt(0) == '(') {
				stack.push(new PjString(PjString.decodePdf(state._token)));
			}
			else if (state._token.charAt(0) == '/') {
				stack.push(new PjName(state._token.substring(1)));
			}
			else if (state._token.equals(">>")) {
				boolean done = false;
				Object obj;
				Hashtable h = new Hashtable();
				while ( ! done ) {
					obj = stack.pop();
					if ( (obj instanceof String) &&
					     (((String)obj).equals("<<")) ) {
						done = true;
					} else {
						h.put((PjName)(stack.pop()),
						      (PjObject)obj);
					}
				}
				// figure out what kind of dictionary we have
				PjDictionary dictionary = new PjDictionary(h);
				if (PjPage.isLike(dictionary)) {
					stack.push(new PjPage(h));
				}
				else if (PjPages.isLike(dictionary)) {
					stack.push(new PjPages(h));
				}
				else if (PjFontType1.isLike(dictionary)) {
					stack.push(new PjFontType1(h));
				}
				else if (PjFontDescriptor.isLike(dictionary)) {
					stack.push(new PjFontDescriptor(h));
				}
				else if (PjResources.isLike(dictionary)) {
					stack.push(new PjResources(h));
				}
				else if (PjCatalog.isLike(dictionary)) {
					stack.push(new PjCatalog(h));
				}
				else if (PjInfo.isLike(dictionary)) {
					stack.push(new PjInfo(h));
				}
				else if (PjEncoding.isLike(dictionary)) {
					stack.push(new PjEncoding(h));
				}
				else {
					stack.push(dictionary);
				}
			}
			else if (state._token.equals("]")) {
				boolean done = false;
				Object obj;
				Vector v = new Vector();
				while ( ! done ) {
					obj = stack.pop();
					if ( (obj instanceof String) &&
					     (((String)obj).equals("[")) ) {
						done = true;
					} else {
						v.insertElementAt((PjObject)obj, 0);
					}
				}
				// figure out what kind of array we have
				PjArray array = new PjArray(v);
				if (PjRectangle.isLike(array)) {
					stack.push(new PjRectangle(v));
				}
				else if (PjProcSet.isLike(array)) {
					stack.push(new PjProcSet(v));
				}
				else {
					stack.push(array);
				}
			}
			else if (state._token.startsWith("%")) {
				// do nothing
			}
			else {
				stack.push(state._token);
			}
		}
		return (PjObject)(stack.pop());
	}

	private static boolean getLine(PdfParserState state) {
		StringBuffer sb = new StringBuffer();
		char c;
		while (state._pos < state._data.length) {
			c = (char)(state._data[state._pos]);
			state._pos++;
			switch (c) {
			case '\r':
				if ( (state._pos < state._data.length) &&
				     ((char)(state._data[state._pos]) == '\n') ) {
					state._pos++;
				}
			case '\n':
				state._token = sb.toString();
				return true;
			default:
				sb.append(c);
			}
		}
		return false;
	}
	
	private static boolean getToken(PdfParserState state) {
		if (state._stream != -1) {
			state._streamToken = new byte[state._stream];
			System.arraycopy(state._data, state._pos, state._streamToken, 0,
					 state._stream);
			state._pos = state._pos + state._stream;
			return true;
		}
		skipWhitespace(state);
		StringBuffer sb = new StringBuffer();
		boolean firstChar = true;
		boolean string = false;
		int stringParen = 0;
		boolean hstring = false;
		char c = '\0';
		char last;
		int x;
		while (state._pos < state._data.length) {
			last = c;
			c = (char)(state._data[state._pos]);
			state._pos++;
			if (firstChar) {
				switch (c) {
				case '(':
					string = true;
					stringParen = 0;
					break;
				case ']':
					state._token = "]";
					return true;
				case '>':
					if ( (state._pos < state._data.length) &&
					     ((char)(state._data[state._pos]) ==
					      '>') ) {
						state._pos++;
						state._token = ">>";
						return true;
					}
					break;
				case '%':
					sb.append('%');
					while ( (state._pos < state._data.length) &&
						((c = (char)(state._data[state._pos])) != '\n') &&
						(c != '\r') ) {
						sb.append(c);
						state._pos++;
					}
					state._token = sb.toString();
					return true;
				default:
				}
			}
			if ( (string) || (hstring) ) {
				if (string) {
					if ( (c == '(') && (last != '\\') ) {
						stringParen++;
					}
					if ( (c == ')') && (last != '\\') ) {
						if (stringParen == 1) {
							sb.append(c);
							state._token = sb.toString();
							return true;
						} else {
							stringParen--;
						}
					}
				} else {
					// hex string
					if (c == '>') {
						sb.append(c);
						state._token = sb.toString();
						return true;
					}
				}
				sb.append(c);
			} else {
				if (isWhitespace(c)) {
					state._token = sb.toString();
					return true;
				} else {
					switch (c) {
					case '[':
						if ( ! firstChar ) {
							state._pos--;
							state._token = sb.toString();
							return true;
						} else {
							state._token = "[";
							return true;
						}
					case '<':
						if ( ! firstChar ) {
							state._pos--;
							state._token = sb.toString();
							return true;
						} else {
							if ( (state._pos < state._data.length) &&
							     ((char)(state._data[state._pos]) ==
							      '<') ) {
								// dictionary
								state._pos++;
								state._token = "<<";
								return true;
							} else {
								// hex string
								hstring = true;
								sb.append(c);
							}
						}
						break;
					case ']':
					case '/':
					case '(':
						if ( ! firstChar ) {
							state._pos--;
							state._token =
								sb.toString();
							return true;
						} else {
							sb.append(c);
							break;
						}
					case '>':
						if ( (state._pos <
						    state._data.length) &&
						     ((char)(state._data[state._pos]) == '>') ) {
							state._pos--;
							state._token =
								sb.toString();
							return true;
						} else {
							sb.append(c);
						}
						break;
					default:
						sb.append(c);
					}
				}
			}
			if (firstChar) {
				firstChar = false;
			}
		}
		return false;
	}

	private static void skipWhitespace(PdfParserState state) {
		while ( (state._pos < state._data.length) && (isWhitespace((char)(state._data[state._pos]))) ) {
			state._pos++;
		}
	}

	private static boolean isWhitespace(char c) {
		switch (c) {
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			return true;
		default:
			return false;
		}
	}

}
