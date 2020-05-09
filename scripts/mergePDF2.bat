@echo off
gswin64c -dNOPAUSE -sDEVICE=pdfwrite -sOUTPUTFILE=merged2.pdf -dBATCH merged.pdf %1
del merged.pdf
ren merged2.pdf merged.pdf
