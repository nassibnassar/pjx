/*
  Copyright (C) Etymon Systems, Inc. <http://www.etymon.com/>
*/

package com.etymon.pjx;

import java.io.*;

/**
   The abstract superclass of classes <code>PdfInteger</code>,
   <code>PdfFloat</code>, and <code>PdfLong</code>.
   @author Nassib Nassar
*/
public abstract class PdfNumber
	extends PdfObject {

	/**
	   Returns the int value of this number.
	   @return the int value.
	 */
	public abstract int getInt();

	/**
	   Returns the long value of this number.
	   @return the long value.
	 */
	public abstract long getLong();

	/**
	   Returns the float value of this number.
	   @return the float value.
	 */
	public abstract float getFloat();

}
