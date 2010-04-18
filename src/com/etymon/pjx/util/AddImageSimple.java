package com.etymon.pjx.util;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import com.etymon.pjx.*;

/**
   Provides a very rudimentary function for adding JPEG images to a
   PDF document.  This works with most JPEG images.
   @author Nassib Nassar
   @deprecated This class will be removed when a more comprehensive
   image class is completed.
*/
public class AddImageSimple {

	protected static final PdfName PDFNAME_CONTENTS = new PdfName("Contents");
	protected static final PdfName PDFNAME_RESOURCES = new PdfName("Resources");
	protected static final PdfName PDFNAME_PROCSET = new PdfName("ProcSet");
	protected static final PdfName PDFNAME_IMAGEC = new PdfName("ImageC");
	protected static final PdfName PDFNAME_PARENT = new PdfName("Parent");
	protected static final PdfName PDFNAME_NAME = new PdfName("Name");
	protected static final PdfName PDFNAME_TYPE = new PdfName("Type");
	protected static final PdfName PDFNAME_XOBJECT = new PdfName("XObject");
	protected static final PdfName PDFNAME_SUBTYPE = new PdfName("Subtype");
	protected static final PdfName PDFNAME_IMAGE = new PdfName("Image");
	protected static final PdfName PDFNAME_FILTER = new PdfName("Filter");
	protected static final PdfName PDFNAME_DCTDECODE = new PdfName("DCTDecode");
	protected static final PdfName PDFNAME_WIDTH = new PdfName("Width");
	protected static final PdfName PDFNAME_HEIGHT = new PdfName("Height");
	protected static final PdfName PDFNAME_BITSPERCOMPONENT = new PdfName("BitsPerComponent");
	protected static final PdfName PDFNAME_COLORSPACE = new PdfName("ColorSpace");
	protected static final PdfName PDFNAME_DEVICERGB = new PdfName("DeviceRGB");
	protected static final PdfInteger PDFINTEGER_8 = new PdfInteger(8);

	/**
	   The manager associated with this document.
	*/
	protected PdfManager _m;

	/**
	   Constructs an <code>AddImageSimple</code> instance based on
	   a specified <code>PdfManager</code>.
	 */
	public AddImageSimple(PdfManager manager) {

		_m = manager;
		
	}

	/**
	   Adds a specified JPEG image (contained in a file) to a page
	   in the PDF document.  This method requires the calling
	   method to specify the original size of the image.
	   @param imageFile the file containing the JPEG image.
	   @param imageWidth the original width of the image.
	   @param imageHeight the original height of the image.
	   @param imageName a name object to associate with the image,
	   for identification purposes.
	   @param page an indirect reference to the page dictionary
	   object that the image will be added to.
	   @param positionX the X location to position the image at.
	   @param positionY the Y location to position the image at.
	   @param scaleX a scaling factor in the X dimension.
	   @param scaleY a scaling factor in the Y dimension.
	   @param background if <code>true</code>, the image will be
	   layered behind the existing page contents rather than on
	   top of it.  This can be used to create simple watermarks.
	   @throws PdfFormatException
	 */
	public void addImage(File imageFile, int imageWidth, int imageHeight,
			     PdfName imageName,
			     PdfReference page,
			     float positionX, float positionY,
			     float scaleX, float scaleY,
			     boolean background) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				FileInputStream fis = new FileInputStream(imageFile);
				FileChannel fc = fis.getChannel();
				ByteBuffer bb = ByteBuffer.allocateDirect((int)fc.size());
				fc.read(bb);
				fc.close();
				fis.close();
				bb.position(0);

				addImage(bb, imageWidth, imageHeight,
					 imageName,
					 page,
					 positionX, positionY,
					 scaleX, scaleY,
					 background);
				
			}
		}
	}
	
	/**
	   Adds a specified JPEG image (contained in a buffer) to a
	   page in the PDF document.  This method requires the calling
	   method to specify the original size of the image.
	   @param image the buffer containing the JPEG image.  This
	   method reads from the current position until there are no
	   more bytes remaining.
	   @param imageWidth the original width of the image.
	   @param imageHeight the original height of the image.
	   @param imageName a name object to associate with the image,
	   for identification purposes.
	   @param page an indirect reference to the page dictionary
	   object that the image will be added to.
	   @param positionX the X location to position the image at.
	   @param positionY the Y location to position the image at.
	   @param scaleX a scaling factor in the X dimension.
	   @param scaleY a scaling factor in the Y dimension.
	   @param background if <code>true</code>, the image will be
	   layered behind the existing page contents rather than on
	   top of it.  This can be used to create simple watermarks.
	   @throws PdfFormatException
	 */
	public void addImage(ByteBuffer image, int imageWidth, int imageHeight,
			     PdfName imageName,
			     PdfReference page,
			     float positionX, float positionY,
			     float scaleX, float scaleY,
			     boolean background) throws IOException, PdfFormatException {
		synchronized (this) {
			synchronized (_m) {

				PdfPageTree pageTree = new PdfPageTree(_m);
				
				// get the page dictionary
				Object obj = _m.getObjectIndirect(page);
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Page object is not a dictionary.");
				}
				PdfDictionary pageExplicit = pageTree.inheritAttributes( (PdfDictionary)obj );
				Map pageMap = new HashMap(pageExplicit.getMap());
				
				// create the image XObject
				Map imageMap = new HashMap(9);
				imageMap.put(PDFNAME_NAME, imageName);
				imageMap.put(PDFNAME_TYPE, PDFNAME_XOBJECT);
				imageMap.put(PDFNAME_SUBTYPE, PDFNAME_IMAGE);
				imageMap.put(PDFNAME_FILTER, PDFNAME_DCTDECODE);
				imageMap.put(PDFNAME_WIDTH, new PdfInteger(imageWidth));
				imageMap.put(PDFNAME_HEIGHT, new PdfInteger(imageHeight));
				imageMap.put(PDFNAME_BITSPERCOMPONENT, PDFINTEGER_8);
				imageMap.put(PDFNAME_COLORSPACE, PDFNAME_DEVICERGB);
				PdfStream imageStream = new PdfStream(new PdfDictionary(imageMap), image);

				// add the image XObject to the
				// document
				int imageId = _m.addObject(imageStream);

				// create a tiny contents stream with
				// only "q" to save the state.  this
				// will go before all streams on the
				// page, to ensure that we can get a
				// clean coordinate matrix
				String tinyString = "q\n";
				byte[] tinyBytes = tinyString.getBytes();
				ByteBuffer tinyBuffer = ByteBuffer.wrap(tinyBytes);
				PdfStream tinyStream = new PdfStream(new PdfDictionary(new HashMap()),
								     tinyBuffer);
				
				// create the image contents stream
				float sX = scaleX * imageWidth;
				float sY = scaleY * imageHeight;
				String contentsString = "Q q " +
					sX + " 0 0 " + sY + ' ' + positionX + ' ' + positionY + " cm " +
					imageName + " Do Q\n";
				byte[] contentsBytes = contentsString.getBytes();
				ByteBuffer contentsBuffer = ByteBuffer.wrap(contentsBytes);
				PdfStream contentsStream = new PdfStream(new PdfDictionary(new HashMap()),
									 contentsBuffer);

				// add the contents streams to the
				// document
				int tinyId = _m.addObject(tinyStream);
				int contentsId = _m.addObject(contentsStream);

				// add references to the contents
				// streams to the page dictionary
				obj = pageMap.get(PDFNAME_CONTENTS);
				List contentList;
				if (obj instanceof PdfArray) {
					contentList = new ArrayList( ((PdfArray)obj).getList() );
				}
				else if (obj instanceof PdfReference) {
					contentList = new ArrayList();
					contentList.add(obj);
				}
				else {
					throw new PdfFormatException(
						"Page content stream is not an array or indirect reference.");
				}
				contentList.add( 0, new PdfReference(tinyId, 0) );
				if (background) {
					contentList.add( 1, new PdfReference(contentsId, 0) );
				} else {
					contentList.add( new PdfReference(contentsId, 0) );
				}
				pageMap.put(PDFNAME_CONTENTS, new PdfArray(contentList) );

				// get the resources dictionary
				obj = pageMap.get(PDFNAME_RESOURCES);
				if (obj == null) {
					throw new PdfFormatException(
						"Resources dictionary is missing from page.");
				}
				if ( !(obj instanceof PdfObject) ) {
					throw new PdfFormatException(
						"Resources dictionary is not a PDF object.");
				}
				obj = _m.getObjectIndirect((PdfObject)obj);
				if ( !(obj instanceof PdfDictionary) ) {
					throw new PdfFormatException(
						"Resources object is not a dictionary.");
				}
				Map resourcesMap = new HashMap( ((PdfDictionary)obj).getMap() );

				// get the procedure set and add ImageC
				obj = resourcesMap.get(PDFNAME_PROCSET);
				Set procsetSet;
				if (obj == null) {
					procsetSet = new HashSet(1);
				} else {
					if ( !(obj instanceof PdfObject) ) {
						throw new PdfFormatException(
							"Procedure set is not a PDF object.");
					}
					obj = _m.getObjectIndirect((PdfObject)obj);
					if ( !(obj instanceof PdfArray) ) {
						throw new PdfFormatException(
							"Procedure set is not an array.");
					}
					procsetSet = new HashSet( ((PdfArray)obj).getList() );
				}
				procsetSet.add(PDFNAME_IMAGEC);
				resourcesMap.put(PDFNAME_PROCSET,
						 new PdfArray( new ArrayList(procsetSet) ) );

				// get the XObject dictionary, or
				// create one
				Map xobjectMap;
				obj = resourcesMap.get(PDFNAME_XOBJECT);
				if (obj == null) {
					xobjectMap = new HashMap();
				} else {
					if ( !(obj instanceof PdfObject) ) {
						throw new PdfFormatException(
							"XObject is not a PDF object.");
					}
					obj = _m.getObjectIndirect((PdfObject)obj);
					if ( !(obj instanceof PdfDictionary) ) {
						throw new PdfFormatException(
							"XObject is not a dictionary.");
					}
					xobjectMap = new HashMap( ((PdfDictionary)obj).getMap() );
				}
				xobjectMap.put(imageName, new PdfReference(imageId, 0));
				resourcesMap.put(PDFNAME_XOBJECT, new PdfDictionary(xobjectMap));

				// set the new resources dictionary
				pageMap.put(PDFNAME_RESOURCES, new PdfDictionary(resourcesMap) );
				
				// update page dictionary
				_m.setObject( new PdfDictionary(pageMap),
					      page.getObjectNumber() );

			}
		}
	}
	
}
