package com.etymon.pj.object;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;

/**
   A PDF object representation.  This is the base class for all
   high level PDF objects.
   @author Nassib Nassar
*/
public abstract class PjObject
	extends BaseObject {

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
	}

}
