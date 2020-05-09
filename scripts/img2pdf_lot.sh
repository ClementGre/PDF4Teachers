#!/bin/bash
for fichier in *.jpg
do
	convert *.jpg -resize 2000000@> test.pdf	  -sOutputFile="PDFcompress/$fichier" "$fichier"
done
