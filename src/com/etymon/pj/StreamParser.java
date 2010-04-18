package com.etymon.pj;

import java.io.*;
import java.util.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;
import com.etymon.pj.object.pagemark.*;

public class StreamParser {

	public StreamParser() {
	}
	
	public Vector parse(PjStream stream) throws PdfFormatException {
		_buffer = stream.getBuffer();
		_stack = new Stack();
		int b;
		_counter = 0;
		_image = false;
		while (_counter < _buffer.length) {
			getToken();
			processToken();
		}
		Vector v = new Vector();
		while ( ! _stack.empty() ) {
			v.insertElementAt(_stack.pop(), 0);
		}
		return v;
	}

	// sets _imageData to the inline image data
	private void getImageData() {
		boolean done = false;
		int start = _counter;
		do {
			if ( ( (_buffer[_counter] == '\n') ||
			       (_buffer[_counter] == '\r') ) &&
			     ((_counter - start) >= 3) ) {
				if ( (_buffer[_counter - 1] == 'I') &&
				     (_buffer[_counter - 2] == 'E') &&
				     ( (_buffer[_counter - 3] == '\n') ||
				       (_buffer[_counter - 3] == '\r') ) ) {
					_imageData = new byte[_counter - start - 2];
					System.arraycopy(_buffer, start, _imageData, 0, _imageData.length);
					done = true;
				}
			}
			_counter++;
		} while (done == false);
		_token = "";
	}
	
	private void getToken() {
		// inline image data (BI-ID-EI) are a special case
		if (_image == true) {
			getImageData();
			return;
		}
		// otherwise do normal processing
		skipWhitespace();
		StringBuffer token = new StringBuffer();
		int stringMode = 0;
		char ch = '\0';
		char lastch = '\0';
		boolean done = false;
		int c;
		while ( (!done) && ((c = _counter) < _buffer.length) ) {
			int b = _buffer[c];
			char oldlastch = lastch;
			lastch = ch;
			ch = (char)b;
			if (stringMode == 0) {
				switch (ch) {
				case '(':
					if (token.length() == 0) {
						stringMode = 1;
						token.append('(');
					} else {
						_counter--;
						done = true;
					}
					break;
				case '[':
				case ']':
					if (token.length() == 0) {
						token.append(ch);
					} else {
						_counter--;
					}
					done = true;
					break;
				case '/':
					if (token.length() == 0) {
						token.append(ch);
					} else {
						_counter--;
						done = true;
					}
					break;
				case '<':
				case '>':
					int x = token.length();
					if (x == 0) {
						token.append(ch);
					} else {
						if ( (x == 1) && (token.charAt(0) == ch) ) {
							token.append(ch);
							done = true;
						} else {
							_counter--;
							done = true;
						}
					}
					break;
				default:
					if (isWhitespace(ch)) {
						done = true;
					} else {
						token.append(ch);
					}
				}
			} else {
				// string mode
				switch (ch) {
				case '(':
					token.append('(');
					if ( (lastch != '\\') || (oldlastch == '\\') ) {
						stringMode++;
					}
					break;
				case ')':
					token.append(')');
					if ( (lastch != '\\') || (oldlastch == '\\') ) {
						stringMode--;
						if (stringMode == 0) {
							done = true;
						}
					}
					break;
				default:
					token.append(ch);
				}
			}
			_counter++;
		}
		_token = token.toString();
	}

	private void skipWhitespace() {
		int c;
		while ( ((c = _counter) < _buffer.length) && (isWhitespace((char)_buffer[c])) ) {
			_counter++;
		}
	}

	private static boolean isWhitespace(char c) {
		return ( (c == ' ') ||
			 (c == '\t') ||
			 (c == '\r') ||
			 (c == '\n') );
	}

	private void processToken() throws PdfFormatException {
		if ( (_token.length() == 0) && (_image == false) ) {
			return;
		}
		if (_image == true) {
			_image = false;
			// create image object
			_o1 = _stack.pop();
			if ( ! (_o1 instanceof PjDictionary) ) {
				throw new PdfFormatException("Dictionary expected before ID.");
			}
			_stack.push(new XEI((PjDictionary)_o1, _imageData));
		}
		else if (_token.equals("BI")) {
			_stack.push("<<");
		}
		else if ( (_token.equals(">>")) || (_token.equals("ID")) ) {
			_b1 = false;
			_h1 = new Hashtable();
			while ( ! _b1 ) {
				_o2 = _stack.pop();
				if ( (_o2 instanceof String) &&
				     (((String)_o2).equals("<<")) ) {
						_b1 = true;
				} else {
					if ( ! (_o2 instanceof PjObject) ) {
						throw new PdfFormatException("PDF object expected within dictionary.");
					}
					_o1 = _stack.pop();
					if ( ! (_o1 instanceof PjName) ) {
						throw new PdfFormatException("Name (key) expected within dictionary.");
					}
					_h1.put(_o1, _o2);
				}
			}
			_stack.push(new PjDictionary(_h1));
			if (_token.equals("ID")) {
				_image = true;
			}
		}
		else if (_token.equals("BT")) {
			_stack.push(new XBT());
		}
		else if (_token.equals("ET")) {
			_stack.push(new XET());
		}
		else if (_token.equals("Td")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (y offset) expected before Td.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (x offset) expected before Td.");
			}
			_stack.push(new XTd(_n1, _n2));
		}
		else if (_token.equals("TD")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (y offset) expected before TD.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (x offset) expected before TD.");
			}
			_stack.push(new XXTD(_n1, _n2));
		}
		else if (_token.equals("m")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (y) expected before m.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (x) expected before m.");
			}
			_stack.push(new Xm(_n1, _n2));
		}
		else if (_token.equals("l")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (y) expected before l.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (x) expected before l.");
			}
			_stack.push(new Xl(_n1, _n2));
		}
		else if (_token.equals("d0")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (w[y]) expected before d0.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (w[x]) expected before d0.");
			}
			_stack.push(new Xd0(_n1, _n2));
		}
		else if (_token.equals("i")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (flatness) expected before i.");
			}
			_stack.push(new Xi(_n1));
		}
		else if (_token.equals("g")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (gray) expected before g.");
			}
			_stack.push(new Xg(_n1));
		}
		else if (_token.equals("G")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (gray) expected before G.");
			}
			_stack.push(new XXG(_n1));
		}
		else if (_token.equals("w")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (line width) expected before w.");
			}
			_stack.push(new Xw(_n1));
		}
		else if (_token.equals("Ts")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (rise) expected before Ts.");
			}
			_stack.push(new XTs(_n1));
		}
		else if (_token.equals("TL")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (leading) expected before TL.");
			}
			_stack.push(new XTL(_n1));
		}
		else if (_token.equals("Tz")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (scale) expected before Tz.");
			}
			_stack.push(new XTz(_n1));
		}
		else if (_token.equals("Tw")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (word space) expected before Tw.");
			}
			_stack.push(new XTw(_n1));
		}
		else if (_token.equals("Tc")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (char space) expected before Tc.");
			}
			_stack.push(new XTc(_n1));
		}
		else if (_token.equals("Tr")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (render) expected before Tr.");
			}
			_stack.push(new XTr(_n1));
		}
		else if (_token.equals("j")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (line join) expected before j.");
			}
			_stack.push(new Xj(_n1));
		}
		else if (_token.equals("J")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (line cap) expected before J.");
			}
			_stack.push(new XXJ(_n1));
		}
		else if (_token.equals("M")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (miter limit) expected before M.");
			}
			_stack.push(new XXM(_n1));
		}
		else if (_token.equals("cm")) {
			_o6 = _stack.pop();
			_o5 = _stack.pop();
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) &&
			     (_o5 instanceof PjNumber) &&
			     (_o6 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
				_n5 = (PjNumber)_o5;
				_n6 = (PjNumber)_o6;
			} else {
				throw new PdfFormatException("Number expected before cm.");
			}
			_stack.push(new Xcm(_n1, _n2, _n3, _n4, _n5, _n6));
		}
		else if (_token.equals("d1")) {
			_o6 = _stack.pop();
			_o5 = _stack.pop();
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) &&
			     (_o5 instanceof PjNumber) &&
			     (_o6 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
				_n5 = (PjNumber)_o5;
				_n6 = (PjNumber)_o6;
			} else {
				throw new PdfFormatException("Number expected before d1.");
			}
			_stack.push(new Xd1(_n1, _n2, _n3, _n4, _n5, _n6));
		}
		else if (_token.equals("c")) {
			_o6 = _stack.pop();
			_o5 = _stack.pop();
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) &&
			     (_o5 instanceof PjNumber) &&
			     (_o6 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
				_n5 = (PjNumber)_o5;
				_n6 = (PjNumber)_o6;
			} else {
				throw new PdfFormatException("Number expected before c.");
			}
			_stack.push(new Xc(_n1, _n2, _n3, _n4, _n5, _n6));
		}
		else if (_token.equals("v")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
			} else {
				throw new PdfFormatException("Number expected before v.");
			}
			_stack.push(new Xv(_n1, _n2, _n3, _n4));
		}
		else if (_token.equals("y")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
			} else {
				throw new PdfFormatException("Number expected before y.");
			}
			_stack.push(new Xy(_n1, _n2, _n3, _n4));
		}
		else if (_token.equals("Tm")) {
			_o6 = _stack.pop();
			_o5 = _stack.pop();
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) &&
			     (_o5 instanceof PjNumber) &&
			     (_o6 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
				_n5 = (PjNumber)_o5;
				_n6 = (PjNumber)_o6;
			} else {
				throw new PdfFormatException("Number expected before Tm.");
			}
			_stack.push(new XTm(_n1, _n2, _n3, _n4, _n5, _n6));
		}
		else if (_token.equals("k")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
			} else {
				throw new PdfFormatException("Number expected before k.");
			}
			_stack.push(new Xk(_n1, _n2, _n3, _n4));
		}
		else if (_token.equals("K")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
			} else {
				throw new PdfFormatException("Number expected before K.");
			}
			_stack.push(new XXK(_n1, _n2, _n3, _n4));
		}
		else if (_token.equals("sc")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			if (_stack.peek() instanceof PjNumber) {
				_o1 = _stack.pop();
			} else {
				_o1 = null;
			}
			if ( ( (_o1 == null) || (_o1 instanceof PjNumber) ) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				if (_o1 == null) {
					_n1 = (PjNumber)_o2;
					_n2 = (PjNumber)_o3;
					_n3 = (PjNumber)_o4;
					_stack.push(new Xsc(_n1, _n2, _n3));
				} else {
					_n1 = (PjNumber)_o1;
					_n2 = (PjNumber)_o2;
					_n3 = (PjNumber)_o3;
					_n4 = (PjNumber)_o4;
					_stack.push(new Xsc(_n1, _n2, _n3, _n4));
				}
			} else {
				throw new PdfFormatException("Number expected before sc.");
			}
		}
		else if (_token.equals("SC")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			if (_stack.peek() instanceof PjNumber) {
				_o1 = _stack.pop();
			} else {
				_o1 = null;
			}
			if ( ( (_o1 == null) || (_o1 instanceof PjNumber) ) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				if (_o1 == null) {
					_n1 = (PjNumber)_o2;
					_n2 = (PjNumber)_o3;
					_n3 = (PjNumber)_o4;
					_stack.push(new XXSC(_n1, _n2, _n3));
				} else {
					_n1 = (PjNumber)_o1;
					_n2 = (PjNumber)_o2;
					_n3 = (PjNumber)_o3;
					_n4 = (PjNumber)_o4;
					_stack.push(new XXSC(_n1, _n2, _n3, _n4));
				}
			} else {
				throw new PdfFormatException("Number expected before SC.");
			}
		}
		else if (_token.equals("scn")) {
			// need to handle this
			if (_stack.peek() instanceof PjName) {
				_stack.pop();
			}
			while (_stack.peek() instanceof PjNumber) {
				_stack.pop();
			}
		}
		else if (_token.equals("rg")) {
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
			} else {
				throw new PdfFormatException("Number expected before rg.");
			}
			_stack.push(new Xrg(_n1, _n2, _n3));
		}
		else if (_token.equals("RG")) {
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
			} else {
				throw new PdfFormatException("Number expected before RG.");
			}
			_stack.push(new XXRG(_n1, _n2, _n3));
		}
		else if (_token.equals("re")) {
			_o4 = _stack.pop();
			_o3 = _stack.pop();
			_o2 = _stack.pop();
			_o1 = _stack.pop();
			if ( (_o1 instanceof PjNumber) &&
			     (_o2 instanceof PjNumber) &&
			     (_o3 instanceof PjNumber) &&
			     (_o4 instanceof PjNumber) ) {
				_n1 = (PjNumber)_o1;
				_n2 = (PjNumber)_o2;
				_n3 = (PjNumber)_o3;
				_n4 = (PjNumber)_o4;
			} else {
				throw new PdfFormatException("Number expected before re.");
			}
			_stack.push(new Xre(_n1, _n2, _n3, _n4));
		}
		else if (_token.equals("Tf")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (font size) expected before Tf.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (font name) expected before Tf.");
			}
			_stack.push(new XTf(_m1, _n1));
		}
		else if (_token.equals("\"")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjString) {
				_s1 = (PjString)(_o1);
			} else {
				throw new PdfFormatException("String (text) expected before \".");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n2 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (a[c]) expected before \".");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (a[w]) expected before \".");
			}
			_stack.push(new Xquot(_n1, _n2, _s1));
		}
		else if (_token.equals("BMC")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (tag) expected before BMC.");
			}
			_stack.push(new XBMC(_m1));
		}
		else if (_token.equals("MP")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (tag) expected before MP.");
			}
			_stack.push(new XMP(_m1));
		}
		else if (_token.equals("gs")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (name) expected before gs.");
			}
			_stack.push(new Xgs(_m1));
		}
		else if (_token.equals("Do")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (XObject) expected before Do.");
			}
			_stack.push(new XDo(_m1));
		}
		else if (_token.equals("cs")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (color space) expected before cs.");
			}
			_stack.push(new Xcs(_m1));
		}
		else if (_token.equals("CS")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjName) {
				_m1 = (PjName)(_o1);
			} else {
				throw new PdfFormatException("Name (color space) expected before CS.");
			}
			_stack.push(new XXCS(_m1));
		}
		else if (_token.equals("d")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjNumber) {
				_n1 = (PjNumber)(_o1);
			} else {
				throw new PdfFormatException("Number (phase) expected before d.");
			}
			_o1 = _stack.pop();
			if (_o1 instanceof PjArray) {
				_a1 = (PjArray)(_o1);
			} else {
				throw new PdfFormatException("Array (dash pattern) expected before d.");
			}
			_stack.push(new Xd(_a1, _n1));
		}
		else if (_token.equals("TJ")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjArray) {
				_a1 = (PjArray)(_o1);
			} else {
				throw new PdfFormatException("Array expected before TJ.");
			}
			_stack.push(new XXTJ(_a1));
		}
		else if (_token.equals("BDC")) {
			_o1 = _stack.pop();
			if ( ( ! (_o1 instanceof PjName) ) &&
			     ( ! (_o1 instanceof PjDictionary) ) ) {
				throw new PdfFormatException("Name or dictionary (property list) expected before BDC.");
			}
			_o2 = _stack.pop();
			if (_o2 instanceof PjName) {
				_m1 = (PjName)(_o2);
			} else {
				throw new PdfFormatException("Name (tag) expected before BDC.");
			}
			if (_o1 instanceof PjName) {
				_stack.push(new XBDC(_m1, (PjName)(_o1)));
			} else {
				_stack.push(new XBDC(_m1, (PjDictionary)(_o1)));
			}
		}
		else if (_token.equals("DP")) {
			_o1 = _stack.pop();
			if ( ( ! (_o1 instanceof PjName) ) &&
			     ( ! (_o1 instanceof PjDictionary) ) ) {
				throw new PdfFormatException("Name or dictionary (property list) expected before DP.");
			}
			_o2 = _stack.pop();
			if (_o2 instanceof PjName) {
				_m1 = (PjName)(_o2);
			} else {
				throw new PdfFormatException("Name (tag) expected before DP.");
			}
			if (_o1 instanceof PjName) {
				_stack.push(new XDP(_m1, (PjName)(_o1)));
			} else {
				_stack.push(new XDP(_m1, (PjDictionary)(_o1)));
			}
		}
		else if (_token.equals("Tj")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjString) {
				_s1 = (PjString)(_o1);
			} else {
				throw new PdfFormatException("String (text) expected before Tj.");
			}
			_stack.push(new XTj(_s1));
		}
		else if (_token.equals("'")) {
			_o1 = _stack.pop();
			if (_o1 instanceof PjString) {
				_s1 = (PjString)(_o1);
			} else {
				throw new PdfFormatException("String (text) expected before '.");
			}
			_stack.push(new Xapost(_s1));
		}
		else if (_token.equals("n")) {
			_stack.push(new Xn());
		}
		else if (_token.equals("s")) {
			_stack.push(new Xs());
		}
		else if (_token.equals("S")) {
			_stack.push(new XXS());
		}
		else if (_token.equals("T*")) {
			_stack.push(new XTstar());
		}
		else if ( (_token.equals("f")) || (_token.equals("F")) ) {
			_stack.push(new Xf());
		}
		else if (_token.equals("f*")) {
			_stack.push(new Xfstar());
		}
		else if (_token.equals("b")) {
			_stack.push(new Xb());
		}
		else if (_token.equals("W")) {
			_stack.push(new XXW());
		}
		else if (_token.equals("W*")) {
			_stack.push(new XWstar());
		}
		else if (_token.equals("b*")) {
			_stack.push(new Xbstar());
		}
		else if (_token.equals("B")) {
			_stack.push(new XXB());
		}
		else if (_token.equals("B*")) {
			_stack.push(new XXBstar());
		}
		else if (_token.equals("h")) {
			_stack.push(new Xh());
		}
		else if (_token.equals("q")) {
			_stack.push(new Xq());
		}
		else if (_token.equals("Q")) {
			_stack.push(new XXQ());
		}
		else if (_token.equals("BX")) {
			_stack.push(new XBX());
		}
		else if (_token.equals("EX")) {
			_stack.push(new XEX());
		}
		else if (_token.equals("EMC")) {
			_stack.push(new XEMC());
		}
		else if (_token.charAt(0) == '/') {
			_stack.push(new PjName(_token.substring(1)));
		}
		else if (
			(Character.isDigit(_token.charAt(0)))
			|| (_token.charAt(0) == '-')
			|| (_token.charAt(0) == '.') ) {
			_stack.push(new PjNumber(new Float(_token).floatValue()));
		}
		else if (_token.charAt(0) == '(') {
			_stack.push(new PjString(PjString.decodePdf(_token)));
		}
		else if (_token.equals("<<")) {
			_stack.push("<<");
		}
		else if (_token.equals("[")) {
			_stack.push("[");
		}
		else if (_token.equals("]")) {
			_b1 = false;
			_v1 = new Vector();
			while ( ! _b1 ) {
				_o1 = _stack.pop();
				if ( (_o1 instanceof String) &&
				     (((String)_o1).equals("[")) ) {
					_b1 = true;
				} else {
					if ( ! (_o1 instanceof PjObject) ) {
						throw new PdfFormatException("PDF object expected within array.");
					}
					_v1.insertElementAt(_o1, 0);
				}
			}
			_stack.push(new PjArray(_v1));
		}
		else if (_token.equals("true")) {
			_stack.push(new PjBoolean(true));
		}
		else if (_token.equals("false")) {
			_stack.push(new PjBoolean(false));
		}
		else if ( (_token.equals("scn")) ||
			  (_token.equals("SCN")) ||
			  (_token.equals("sh")) ||
			  (_token.equals("ri")) ) {
			// temporary fix for these operators
			_stack.pop();
		}
		else if (_token.startsWith("%%")) {
			// do nothing
		}
		else {
			throw new PdfFormatException("Token \"" + _token + "\" not recognized.");
		}
	}

	private String _token;
	private byte[] _imageData;
	private Stack _stack;
	private int _counter;
	private byte[] _buffer;
	private boolean _image;

	private PjNumber _n1, _n2, _n3, _n4, _n5, _n6;
	private Stack _k1;
	private boolean _b1;
	private int _x1, _x2;
	private Vector _v1;
	private Object _o1, _o2, _o3, _o4, _o5, _o6;
	private PjName _m1;
	private PjString _s1;
	private PjArray _a1;
	private Hashtable _h1;

}
