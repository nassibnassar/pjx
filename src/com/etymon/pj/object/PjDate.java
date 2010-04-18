package com.etymon.pj.object;

import java.io.*;
import com.etymon.pj.exception.*;

/**
   A representation of the PDF Date type.
   @author Nassib Nassar
*/
public class PjDate
	extends PjString {

	/**
	   Creates a Date object.
	   @param s the string value to initialize this object to.
	*/
	public PjDate(String s) {
		super(s);
	}

	// this should be added (or something like this).
	// this would encode the date as a string.
	/*
	public PjDate(Date d) {
	}
	*/

	// this should be added, similar to isLike in other classes in this package.
	/*
	public static boolean isLike(PjString s) {
	}
	*/
	
}
