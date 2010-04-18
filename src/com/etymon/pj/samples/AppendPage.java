package com.etymon.pj.samples;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;

/**
   Demonstrates appending a page to the end of a PDF file.
   @author Nassib Nassar
 */
public class AppendPage {

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println("AppendPage [infile] [outfile]");
			return;
		}
		String infile = args[0];
		String outfile = args[1];
		System.out.println("This appends a new page with a short message to the end of");
		System.out.println("the pdf document.");
		try {
			System.out.println("Reading and parsing input file...");
			
			// load the infile
			Pdf pdf = new Pdf(infile);

			System.out.println("Got it.");

			// create a font object
			PjFontType1 font = new PjFontType1();
			font.setBaseFont(new PjName("Helvetica-Bold"));
			font.setEncoding(new PjName("PDFDocEncoding"));

			int fontId = pdf.registerObject(font);

			// create a resources dictionary
			PjResources resources = new PjResources();
			// add ProcSet
			Vector procsetVector = new Vector();
			procsetVector.addElement(new PjName("PDF"));
			procsetVector.addElement(new PjName("Text"));
			resources.setProcSet(new PjProcSet(procsetVector));
			// add Font
			Hashtable fontResHt = new Hashtable();
			fontResHt.put(new PjName("F1"),
				       new PjReference(new PjNumber(fontId)));
			resources.setFont(new PjDictionary(fontResHt));
			int resourcesId = pdf.registerObject(resources);

			// add text
			String s = new String("BT\n/F1 24 Tf\n72 720 Td\n(Hello, world!) Tj\nET\n");
			byte[] data = s.getBytes();
			PjStream stream = new PjStream(data);
			int streamId = pdf.registerObject(stream);
			
			// create a new page
			PjPage page = new PjPage();
			page.setResources( new PjReference(
				new PjNumber(resourcesId),
				PjNumber.ZERO ) );
			pdf.addToPage(page, streamId);
			int pageId = pdf.registerObject(page);

			pdf.appendPage(pageId);

			System.out.println("Writing modified output file...");

			pdf.writeToFile(outfile);

			System.out.println("Done.");
		}
		catch (PjException pje) {
			System.out.println(pje);
		}
		catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
}
