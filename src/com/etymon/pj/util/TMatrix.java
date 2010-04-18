package com.etymon.pj.util;

public class TMatrix {

	// all methods assume a valid 3x3 matrix!
	
	/**
	   Returns a new transformation matrix, initialized to the identity matrix.
	*/
	public static float[][] init() {
		float[][] id = { {1, 0, 0},
				 {0, 1, 0},
				 {0, 0, 1} };
		return id;
	}

	public static String toString(float[][] m) {
		return "{ {" + m[0][0] + ", " + m[0][1] + ", " + m[0][2] + "},\n" +
			"  {" + m[1][0] + ", " + m[1][1] + ", " + m[1][2] + "},\n" +
			"  {" + m[2][0] + ", " + m[2][1] + ", " + m[2][2] + "} }";
	}
	
	public static float[][] toMatrix(float a, float b, float c, float d, float x, float y) {
		float[][] m = init();
		m[0][0] = a;
		m[0][1] = b;
		m[1][0] = c;
		m[1][1] = d;
		m[2][0] = x;
		m[2][1] = y;
		return m;
	}

	public static float[][] clone(float[][] m) {
		float[][] n = init();
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				n[r][c] = m[r][c];
			}
		}
		return n;
	}
	
	public static float[][] toMatrix(float x, float y) {
		float[][] m = init();
		m[2][0] = x;
		m[2][1] = y;
		return m;
	}

	public static float[][] multiply(float[][] a, float[][] b) {
		if (a[0].length != b.length) {
			return null;
		}
		float[][] c = new float[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int k = 0; k < b[0].length; k++) {
				float s = 0;
				for (int j = 0; j < b.length; j++) {
					s = s + a[i][j] * b[j][k];
				}
				c[i][k] = s;
			}
		}
		return c;
	}
	
	public static float[][] IDENTITY = { {1, 0, 0},
					     {0, 1, 0},
					     {0, 0, 1} };

}
