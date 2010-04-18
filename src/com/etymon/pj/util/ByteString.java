package com.etymon.pj.util;

public class ByteString {

	public static int lastIndexOf(byte[] buffer, String str) {
		// for now, brute force
		if (str.length() == 0) {
			return buffer.length;
		}
		int length = str.length();
		int x, y;
		boolean match;
		for (x = (buffer.length - length); x >= 0; x--) {
			match = true;
			for (y = 0; y < length; y++) {
				if ((char)(buffer[x + y]) !=
				    str.charAt(y)) {
					match = false;
					break;
				}
			}
			if (match) {
				return x;
			}
		}
		return -1;
	}

}
