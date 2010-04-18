SHOW = -public
#SHOW = -private -linksource
PJX=${HOME}/cvs/pjx

JAVADOC_HEADER = <img src="http://www.etymon.com/resources/images/pjx_50x50.gif" border=0 alt="PJX">

all: pjx.pdf
#all: pjx.pdf pjx.html

jd:
	make clean_javadoc
	javadoc -classpath ${PJX}/src \
		-d javadoc \
		-sourcepath ../src \
		-subpackages com.etymon.pjx \
		${SHOW} \
		-group 'PJX' 'com.etymon.pjx:com.etymon.pjx.*' \
		-windowtitle 'PJX' \
		-header '<a href="http://www.etymon.com/epub.html">${JAVADOC_HEADER}</a>' \
		-footer '<font size="-2"><i>Copyright &copy; 1998-2004 Etymon Systems, Inc.<p> \
		The PDF data structures, operators, and specification are<br> \
		Copyright &copy; 1985-2004 Adobe Systems Incorporated.</i></font>' \
		-doctitle "PJX, v `cat ../VERSION`<br>API Specification</h2><p> \
		<i>Nassib Nassar&nbsp;&nbsp;<br>Etymon Systems, Inc.</i><h2>" \
		-overview ../src/overview.html \
		-breakiterator \
		-link http://java.sun.com/j2se/1.4.1/docs/api \
		-use
#		-subpackages com.etymon.pjx com.etymon.pj \
#		-group 'PJ (deprecated)' 'com.etymon.pj:com.etymon.pj.*' \
#		-linkoffline http://java.sun.com/j2se/1.4.1/docs/api . \
#		-exclude com.etymon.pjx.old:com.etymon.pjx.veryold \
#		-group 'PJ' 'com.etymon.pj:com.etymon.pj.*' \
#		-header '<a href="http://www.etymon.com/pjx/">PJX</a>' \


TEXI_INCLUDE = version.texi

pjx.pdf: pjx.texi ${TEXI_INCLUDE}
	texi2dvi --pdf pjx.texi

pjx.html: pjx.texi ${TEXI_INCLUDE}
	makeinfo --force --html -I. pjx.texi -o pjx.html

version.texi: ../VERSION
	echo "@set VERSION `cat ../VERSION`" > version.texi


clean:
	make clean_javadoc
	make clean_texi

clean_javadoc:
	rm -rf javadoc/*

clean_texi:
	rm -f *.aux *.cp *.cps *.fn *.fns *.ky *.log *.pg *.tmp *.toc *.tp *.vr *.vrs

distclean:
	make clean
	rm -f *~ *#
