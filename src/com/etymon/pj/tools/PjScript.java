package com.etymon.pj.tools;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;

/**
   Implements a PDF scripting language.

   Possible result codes are 0 (scripts executed normally), 1 (error
   reading script), 2 (syntax error in script), and 3 (error executing
   script command).
   
   @author Nassib Nassar
*/
public class PjScript {

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("pjscript [script_file] [script_arguments]");
			return;
		}

		// open script file
		FileReader fr;
		BufferedReader br = null;
		try {
			fr = new FileReader(args[0]);
			br = new BufferedReader(fr);
		}
		catch (IOException e) {
			System.out.println(new PjScriptException("No such file or directory.",
								 -1, args[0], 1).getFullMessage());
			Runtime.getRuntime().exit(1);
		}
		String[] scriptArgs = new String[args.length - 1];
		System.arraycopy(args, 1, scriptArgs, 0, scriptArgs.length);
		try {
			script(args[0], br, scriptArgs);
		}
		catch (PjScriptException e) {
			System.out.println(e.getFullMessage());
			Runtime.getRuntime().exit(e.getErrorType());
		}
	}

	/**
	      Creates or modifies PDF files based on a script.
	      @param source the file or program name where the script
	      originates.  This is used in printing error messages.
	      @param br the input stream containing the script.
	      @param args the arguments to the script.
	      @return the resultant PDF document.
	      @exception PjScriptException if an error occurs.
	*/
	public static Pdf script(String source, BufferedReader br, String[] args) throws PjScriptException {
		// read and process script commands
		String line;
		StringTokenizer tokenizer;
		String command;
		String endLine;
		Pdf pdf = new Pdf();
		int lineNumber = 0;
		boolean good;
		Hashtable vars = new Hashtable();
		Vector texts = new Vector();
		texts.setSize(2);
		Hashtable fonts = new Hashtable();
		// initialize some pjscript variables
		if (args != null) {
			for (int x = 0; x < args.length; x++) {
				vars.put("arg" + new Integer(x).toString(), args[x]);
			}
		}
		vars.put("fontsize", "10");
		vars.put("font", "Courier");
		vars.put("linewidth", "1");
		vars.put("mediabox", "letter-portrait");
		vars.put("page", "1");
		vars.put("xinit", "72");
		vars.put("x", vars.get("xinit"));
		vars.put("x0", "0");
		vars.put("x1", "0");
		vars.put("yinit", "720");
		vars.put("y", vars.get("yinit"));
		vars.put("y0", "0");
		vars.put("y1", "0");
		vars.put("ylimit", "72");
		do {
			// get a line from the script file
			try {
				line = br.readLine();
			}
			catch (IOException e) {
				throw new PjScriptException("I/O error reading input stream.", -1, source, 1);
			}
			if (line != null) {
				lineNumber++;
				// parse out the command at the beginning of the line
				tokenizer = new StringTokenizer(line);
				if (tokenizer.hasMoreTokens()) {
					command = tokenizer.nextToken();
				} else {
					command = new String();
				}
				// parse out the argument portion of the line
				endLine = getEndLine(line);
				// process command
				good = false;
				if (command.equals("")) {
					good = true;
				}
				if (command.startsWith("#")) {
					good = true;
				}
				if (command.equals("newpdf")) {
					good = true;
					pdf = new Pdf();
					texts = new Vector();
					texts.setSize(2);
					fonts = new Hashtable();
					// set the media box
					String mediaBox = (String)(vars.get("mediabox"));
					if (mediaBox == null) {
						throw new PjScriptException("Media box was not specified.", lineNumber,
									    source, 3);
					}
					float yinit = setMediaBox(pdf, 1, mediaBox, source, lineNumber);
					vars.put("yinit", new PjNumber(yinit).toString());
					vars.put("y", vars.get("yinit"));
				}
				if ( (command.startsWith("$")) && (command.length() > 1) ) {
					good = true;
					if (endLine.length() == 0) {
						throw new PjScriptException("Missing argument.", lineNumber, source, 2);
					}
					String value;
					if (endLine.charAt(0) == '\"') {
						if (endLine.charAt(endLine.length() - 1) != '\"') {
							throw new PjScriptException("'\"' expected.", lineNumber,
										    source, 2);
						}
						value = endLine.substring(1, endLine.length() - 1);
					} else {
						value = endLine;
					}
					vars.put(command.substring(1), value);
				}
				if ( (command.startsWith("+")) && (command.length() > 1) ) {
					good = true;
					if (endLine.length() == 0) {
						throw new PjScriptException("Missing argument.", lineNumber, source, 2);
					}
					String value;
					if (endLine.charAt(0) == '\"') {
						if (endLine.charAt(endLine.length() - 1) != '\"') {
							throw new PjScriptException("'\"' expected.", lineNumber,
										    source, 2);
						}
						value = endLine.substring(1, endLine.length() - 1);
					} else {
						value = endLine;
					}
					try {
						float sum =
							new Float((String)(vars.get(command.substring(1)))).floatValue()
							+ new Float(value).floatValue();
						vars.put(command.substring(1), new PjNumber(sum).toString());
					}
					catch (NumberFormatException e) {
						throw new PjScriptException("Argument must be numeric.",
									    lineNumber, source, 2);
					}
				}
				if ( (command.startsWith("=")) && (command.length() > 1) ) {
					good = true;
					if (endLine.length() == 0) {
						throw new PjScriptException("Missing argument.", lineNumber, source, 2);
					}
					String value = (String)(vars.get(endLine));
					if (value == null) {
						throw new PjScriptException("Undefined variable.", lineNumber, source, 3);
					}
					vars.put(command.substring(1), value);
				}
				if ( (command.startsWith("?")) && (command.length() > 1) ) {
					good = true;
					String value = (String)(vars.get(command.substring(1)));
					if (value == null) {
						throw new PjScriptException("Undefined variable.", lineNumber, source, 3);
					}
					System.out.print(value);
				}
				if (command.equals("dump")) {
					good = true;
					String key;
					for (Enumeration m = vars.keys(); m.hasMoreElements();) {
						key = (String)(m.nextElement());
						System.out.println(key + ": \"" +
								   (String)(vars.get(key)) + "\"");
					}
				}
				if (command.equals("print")) {
					good = true;
					if ( ! quoted(endLine) ) {
						throw new PjScriptException("'\"' expected.", lineNumber, source, 2);
					}
					System.out.print(endLine.substring(1, endLine.length() - 1));
				}
				if (command.equals("println")) {
					good = true;
					if ( ! quoted(endLine) ) {
						throw new PjScriptException("'\"' expected.", lineNumber, source, 2);
					}
					System.out.println(endLine.substring(1, endLine.length() - 1));
				}
				if (command.equals("readpdf")) {
					good = true;
					String fn = (String)(vars.get("file"));
					if (fn == null) {
						throw new PjScriptException("File was not specified.", lineNumber,
									    source, 3);
					} else {
						try {
							pdf = new Pdf(fn);
						}
						catch (IOException ioe) {
							throw new PjScriptException("Unable to read PDF file.",
										    lineNumber, source, 3);
						}
						catch (PjException pje) {
							throw new PjScriptException("Error parsing PDF file.",
										    lineNumber, source, 3);
						}
					}
					texts = new Vector();
					try {
						texts.setSize(pdf.getPageCount() + 1);
					}
					catch (InvalidPdfObjectException e) {
							throw new PjScriptException("PDF error: " + e.getMessage(),
										    lineNumber, source, 3);
					}
					fonts = new Hashtable();
				}
				if (command.equals("appendpdf")) {
					good = true;
					String fn = (String)(vars.get("file"));
					if (fn == null) {
						throw new PjScriptException("File was not specified.", lineNumber,
									    source, 3);
					} else {
						try {
							Pdf other = new Pdf(fn);
							pdf.appendPdfDocument(other);
						}
						catch (IOException ioe) {
							throw new PjScriptException("Unable to read PDF file.",
										    lineNumber, source, 3);
						}
						catch (PjException pje) {
							throw new PjScriptException("Error parsing PDF file.",
										    lineNumber, source, 3);
						}
					}
					texts = new Vector();
					try {
						texts.setSize(pdf.getPageCount() + 1);
					}
					catch (InvalidPdfObjectException e) {
							throw new PjScriptException("PDF error: " + e.getMessage(),
										    lineNumber, source, 3);
					}
					fonts = new Hashtable();
				}
				if (command.equals("appendpage")) {
					good = true;
					int pageNumber;
					try {
						pageNumber = appendPage(pdf, texts, vars);
					}
					catch (PjException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					// set the media box
					String mediaBox = (String)(vars.get("mediabox"));
					if (mediaBox == null) {
						throw new PjScriptException("Media box was not specified.", lineNumber,
									    source, 3);
					}
					float yinit = setMediaBox(pdf, pageNumber, mediaBox, source, lineNumber);
					vars.put("yinit", new PjNumber(yinit).toString());
					vars.put("y", vars.get("yinit"));
				}
				if (command.equals("deletepage")) {
					good = true;
					String page = (String)(vars.get("page"));
					int pageCount;
					try {
						pageCount = pdf.getPageCount();
					}
					catch (InvalidPdfObjectException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					if (pageCount <= 0) {
						throw new PjScriptException("No pages to delete.",
									    lineNumber, source, 3);
					}
					if (pageCount == 1) {
						throw new PjScriptException("Cannot delete the only page.",
									    lineNumber, source, 3);
					}
					int pageNumber = Integer.parseInt(page);
					try {
						pdf.deletePage(pageNumber);
					}
					catch (IndexOutOfBoundsException e) {
						throw new PjScriptException("Page number out of range.",
									    lineNumber, source, 3);
					}
					catch (InvalidPdfObjectException pe) {
						throw new PjScriptException("PDF error: " + pe.getMessage(),
									    lineNumber, source, 3);
					}
				}
				if (command.equals("writepdf")) {
					good = true;
					String fn = (String)(vars.get("file"));
					if (fn == null) {
						throw new PjScriptException("File was not specified.", lineNumber,
									    source, 3);
					} else {
						// draw texts on the PDF file
						drawText(pdf, source, lineNumber, fonts, texts);

						// write it out
						if (fn.length() > 0) {
							try {
								pdf.writeToFile(fn);
							}
							catch (IOException ioe) {
								throw new PjScriptException("Unable to write PDF file.",
											    lineNumber, source, 3);
							}
						}
					}
				}
				if (command.equals("initxy")) {
					good = true;
					vars.put("x", vars.get("xinit"));
					vars.put("y", vars.get("yinit"));
				}
				if (command.equals("setinfo")) {
					good = true;
					String key = (String)(vars.get("key"));
					if (key == null) {
						throw new PjScriptException("Key was not specified.",
									    lineNumber, source, 3);
					}
					String text = (String)(vars.get("text"));
					if (text == null) {
						throw new PjScriptException("Text was not specified.",
									    lineNumber, source, 3);
					}
					// get the Info dictionary
					PjReference infoRef;
					try {
						infoRef = pdf.getInfoDictionary();
					}
					catch (InvalidPdfObjectException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					PjInfo info;
					if (infoRef == null) {
						// create a new Info dictionary and add it
						info = new PjInfo();
						int infoId = pdf.registerObject(info);
						infoRef = new PjReference(new PjNumber(infoId), PjNumber.ZERO);
						pdf.setInfoDictionary(infoRef);
					} else {
						PjDictionary d =
							(PjDictionary)(pdf.getObject(infoRef.getObjNumber().getInt()));
						info = new PjInfo(d.getHashtable());
					}
					// set the new value
					info.getHashtable().put(new PjName(key), new PjString(text));
				}
				if (command.equals("getinfo")) {
					good = true;
					String key = (String)(vars.get("key"));
					if (key == null) {
						throw new PjScriptException("Key was not specified.",
									    lineNumber, source, 3);
					}
					// get the Info dictionary
					PjReference infoRef;
					try {
						infoRef = pdf.getInfoDictionary();
					}
					catch (InvalidPdfObjectException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					// in case nothing is found, set text to ""
					vars.put("text", "");
					// retrieve the value
					PjInfo info;
					if (infoRef != null) {
						PjDictionary d;
						try {
							d = (PjDictionary)(pdf.getObject(
									infoRef.getObjNumber().getInt()));
						}
						catch (ClassCastException e) {
							throw new PjScriptException(
								"PDF error: Info object is not a dictionary.",
								lineNumber, source, 3);
						}
						info = new PjInfo(d.getHashtable());
						PjString str;
						try {
							str = (PjString)(info.getHashtable().get(new PjName(key)));
						}
						catch (ClassCastException e) {
							throw new PjScriptException(
								"PDF error: Field in info object is not a string.",
								lineNumber, source, 3);
						}
						if (str != null) {
							vars.put("text", str.getString());
						}
					}
				}
				if (command.equals("nextxy")) {
					good = true;
					vars.put("x", vars.get("xinit"));
					float y = new Float((String)(vars.get("y"))).floatValue() -
						new Float((String)(vars.get("fontsize"))).floatValue();
					if (y < new Float((String)(vars.get("ylimit"))).floatValue()) {
						int page = Integer.parseInt((String)(vars.get("page")));
						int pageCount;
						try {
							pageCount = pdf.getPageCount();
						}
						catch (InvalidPdfObjectException e) {
							throw new PjScriptException("PDF error: " + e.getMessage(),
										    lineNumber, source, 3);
						}
						if (page == pageCount) {
							int pageNumber;
							try {
								pageNumber = appendPage(pdf, texts, vars);
							}
							catch (PjException e) {
								throw new PjScriptException("PDF error: " + e.getMessage(),
											    lineNumber, source, 3);
							}
							// set the media box
							String mediaBox = (String)(vars.get("mediabox"));
							if (mediaBox == null) {
								throw new PjScriptException("Media box was not specified.",
											    lineNumber,
											    source, 3);
							}
							float yinit = setMediaBox(pdf, pageNumber, mediaBox, source,
										lineNumber);
							vars.put("yinit", new PjNumber(yinit).toString());
						} else {
							vars.put("page", new Integer(page + 1).toString());
						}
						vars.put("y", vars.get("yinit"));
					} else {
						vars.put("y", new PjNumber(y).toString());
					}
				}
				if (command.equals("drawtext")) {
					good = true;
					String text = (String)(vars.get("text"));
					if (text == null) {
						throw new PjScriptException("Text was not specified.",
									    lineNumber, source, 3);
					}
					String page = (String)(vars.get("page"));
					String x = (String)(vars.get("x"));
					String y = (String)(vars.get("y"));
					String font = (String)(vars.get("font"));
					String fontsize = (String)(vars.get("fontsize"));
					int pageNumber = Integer.parseInt(page);
					int pageCount;
					try {
						pageCount = pdf.getPageCount();
					}
					catch (InvalidPdfObjectException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					if ( (pageNumber < 1) || (pageNumber > pageCount) ) {
						throw new PjScriptException("Page number out of range.",
									    lineNumber, source, 3);
					}
					StringBuffer sb = (StringBuffer)(texts.elementAt(pageNumber));
					if (sb == null) {
						sb = new StringBuffer();
						texts.setElementAt(sb, pageNumber);
					}
					sb.append("BT\n/Pj" + font + " " + fontsize + " Tf\n" + x + " " + y +
						  " Td\n" +
						  "0 Tc\n" +
						  // kag - force black
						  "/DeviceGray cs\n0 sc\n" +	
						  "(" + text + ") Tj\nET\n");
					if (fonts.get(font) == null) {
						fonts.put(font, font);
					}
				}
				if (command.equals("drawline")) {
					good = true;
					String page = (String)(vars.get("page"));
					String x0 = (String)(vars.get("x0"));
					String y0 = (String)(vars.get("y0"));
					String x1 = (String)(vars.get("x1"));
					String y1 = (String)(vars.get("y1"));
					String linewidth = (String)(vars.get("linewidth"));
					int pageNumber = Integer.parseInt(page);
					int pageCount;
					try {
						pageCount = pdf.getPageCount();
					}
					catch (InvalidPdfObjectException e) {
						throw new PjScriptException("PDF error: " + e.getMessage(),
									    lineNumber, source, 3);
					}
					if ( (pageNumber < 1) || (pageNumber > pageCount) ) {
						throw new PjScriptException("Page number out of range.",
									    lineNumber, source, 3);
					}
					StringBuffer sb = (StringBuffer)(texts.elementAt(pageNumber));
					if (sb == null) {
						sb = new StringBuffer();
						texts.setElementAt(sb, pageNumber);
					}
					sb.append(linewidth + " w\n" + x0 + ' ' + y0 + " m\n" + x1 + ' ' + y1 + " l\nS\n");
				}
				if (good == false) {
					throw new PjScriptException("Unknown command.", lineNumber, source, 2);
				}
			}
		} while (line != null);
		try {
			br.close();
		}
		catch (IOException e) {
		}
		return pdf;
	}


	private static void drawText(Pdf pdf, String source, int lineNumber, Hashtable fonts, Vector texts)
		throws PjScriptException {
		int pageCount;
		try {
			pageCount = pdf.getPageCount();
		}
		catch (InvalidPdfObjectException e) {
			throw new PjScriptException("PDF error: " + e.getMessage(),
						    lineNumber, source, 3);
		}
		for (int x = 1; x <= pageCount; x++) {
			StringBuffer sb = (StringBuffer)(texts.elementAt(x));
			if (sb != null) {
				
				PjPage page;
				PjResources resources;
				try {
					page =
						(PjPage)(pdf.getObject(pdf.getPage(x)));
					// create a resources dictionary
					resources = (PjResources)(pdf.resolve(
						page.getResources()));
					if (resources == null) {
						Vector v = new Vector();
						v.addElement(PjName.PDF);
						v.addElement(PjName.TEXT);
						PjProcSet procSet = new PjProcSet(v);
						resources = new PjResources();
						resources.setProcSet(procSet);
						page.setResources(resources);
					}
				}
				catch (InvalidPdfObjectException e) {
					throw new PjScriptException("PDF error: " +
								    e.getMessage(),
								    lineNumber, source, 3);
				}
				
				// add Font
				PjDictionary fontDictionary;
				try {
					fontDictionary = (PjDictionary)(pdf.resolve(
						resources.getFont()));
				}
				catch (InvalidPdfObjectException e) {
					throw new PjScriptException("PDF error: " +
								    e.getMessage(),
								    lineNumber, source, 3);
				}
				if (fontDictionary == null) {
					fontDictionary = new PjDictionary();
					resources.setFont(fontDictionary);
				}
				Hashtable fontResHt = fontDictionary.getHashtable();
				// create font objects
				// I know this is not efficient, but we're not
				// talking about very much data
				for (Enumeration m = fonts.keys(); m.hasMoreElements();) {
					String name = (String)(m.nextElement());
					PjFontType1 font = new PjFontType1();
					font.setBaseFont(new PjName(name));
					font.setEncoding(new PjName("WinAnsiEncoding"));
					int fontId = pdf.registerObject(font);
					fontResHt.put(new PjName("Pj" + name),
						      new PjReference(
							      new PjNumber(fontId)));
				}
				int resourcesId = pdf.registerObject(resources);
				
				// add text
				byte[] data = sb.toString().getBytes();
				try {
					PjStream stream =
						new PjStream(data).flateCompress();
					int streamId = pdf.registerObject(stream);
					pdf.addToPage(page, streamId);
				}
				catch (InvalidPdfObjectException e) {
					throw new PjScriptException(
						"PDF error: " + e.getMessage(),
						lineNumber, source, 3);
				}
			}
		}
	}

	private static String getEndLine(String line) {
		int x = 0;
		int length = line.length();
		// move past command
		while ( (x < length) && (Character.isWhitespace(line.charAt(x)) == false) ) {
			x++;
		}
		// move past whitespace
		while ( (x < length) && (Character.isWhitespace(line.charAt(x)) == true) ) {
			x++;
		}
		return line.substring(x, length).trim();
	}

	private static boolean quoted(String s) {
		int length = s.length();
		if (length < 2) {
			return false;
		}
		return ( (s.charAt(0) == '\"') && (s.charAt(length - 1) == '\"') );
	}

	// returns page number of new page
	private static int appendPage(Pdf pdf, Vector texts, Hashtable vars) throws PjException {
		// create a font object
		PjFontType1 font = new PjFontType1();
		font.setBaseFont(new PjName("Helvetica-Bold"));
		font.setEncoding(new PjName("WinAnsiEncoding"));
		
		// create a resources dictionary
		PjResources resources = new PjResources();
		// add ProcSet
		Vector procsetVector = new Vector();
		procsetVector.addElement(new PjName("PDF"));
		procsetVector.addElement(new PjName("Text"));
		resources.setProcSet(new PjProcSet(procsetVector));
		int resourcesId = pdf.registerObject(resources);
		
		// create a new page
		PjPage page = new PjPage();
		page.setResources( new PjReference(
			new PjNumber(resourcesId),
			PjNumber.ZERO ) );
		int pageId = pdf.registerObject(page);
		
		pdf.appendPage(pageId);
		int pageCount;
		pageCount = pdf.getPageCount();
		texts.setSize(pageCount + 1);
		vars.put("page", new Integer(pageCount).toString());
		return pageCount;
	}

	// returns starting y value to be used as yinit
	private static float setMediaBox(Pdf pdf, int pageNumber, String mediaBox,
					String source, int lineNumber) throws PjScriptException {
		int pageId;
		try {
			pageId = pdf.getPage(pageNumber);
		}
		catch (IndexOutOfBoundsException a) {
			// pageNumber is assumed to be in bounds
			return -1;
		}
		catch (InvalidPdfObjectException b) {
			throw new PjScriptException("PDF error: " + b.getMessage(),
						    lineNumber, source, 3);
		}
		PjPage page = (PjPage)(pdf.getObject(pageId));
		PjRectangle rect = getMediaBoxArray(mediaBox);
		page.setMediaBox(rect);
		return ((PjNumber)(rect.getVector().lastElement())).getFloat() - 72;
	}

	private static PjRectangle getMediaBoxArray(String mediaBox) {
		Vector v = new Vector();
		if (mediaBox.equalsIgnoreCase("legal-portrait")) {
			v.addElement(PjNumber.ZERO);
			v.addElement(PjNumber.ZERO);
			v.addElement(new PjNumber(612));
			v.addElement(new PjNumber(1008));
			return new PjRectangle(v);
		}
		if (mediaBox.equalsIgnoreCase("legal-landscape")) {
			v.addElement(PjNumber.ZERO);
			v.addElement(PjNumber.ZERO);
			v.addElement(new PjNumber(1008));
			v.addElement(new PjNumber(612));
			return new PjRectangle(v);
		}
		if (mediaBox.equalsIgnoreCase("letter-landscape")) {
			v.addElement(PjNumber.ZERO);
			v.addElement(PjNumber.ZERO);
			v.addElement(new PjNumber(792));
			v.addElement(new PjNumber(612));
			return new PjRectangle(v);
		}
		// default to letter-portrait
		v.addElement(PjNumber.ZERO);
		v.addElement(PjNumber.ZERO);
		v.addElement(new PjNumber(612));
		v.addElement(new PjNumber(792));
		return new PjRectangle(v);
	}
	
}
