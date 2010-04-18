package com.etymon.pj.samples;

import java.io.*;
import java.util.*;
import com.etymon.pj.*;
import com.etymon.pj.exception.*;
import com.etymon.pj.object.*;

/**
   Demonstrates uncompressing flate-compressed streams in a PDF file.
   @author Nassib Nassar
 */
public class UncompressPdf {

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println("UncompressPdf [infile] [outfile]");
			return;
		}
		String infile = args[0];
		String outfile = args[1];
		System.out.println("This creates a new version of a PDF file and uncompresses");
		System.out.println("any flate-compressed streams.");
		try {
			System.out.println("Reading and parsing input file...");
			
			// load the infile
			Pdf pdf = new Pdf(infile);

			System.out.println("Got it.");

			if (pdf.getEncryptDictionary() != null) {
				System.out.println("File appears to be encrypted.");
			}
			
			int y = pdf.getMaxObjectNumber();

			for (int x = 1; x <= y; x++) {
				PjObject obj = pdf.getObject(x);
				if (obj instanceof PjStream) {
					try {
						pdf.registerObject(((PjStream)obj).flateDecompress(), x);
					}
					catch (InvalidPdfObjectException e) {
					}
				}

			}
			
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
