#!/bin/bash
FILES='ls *.pdf'
LIST=${FILES:2}
gs -dNOPAUSE -sDEVICE=pdfwrite -sOUTPUTFILE=merged.pdf -dBATCH $LIST
