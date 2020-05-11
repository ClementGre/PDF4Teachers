@echo off
echo ------------------------------------------
echo Fusion des PDF d'un dossier en %1
echo ------------------------------------------
echo -

setlocal enabledelayedexpansion enableextensions
set LIST=
for %%x in (*.pdf) do set LIST=!LIST! %%x
set LIST=%LIST:~1%
gswin64c -dNOPAUSE -sDEVICE=pdfwrite -sOUTPUTFILE=%1 -dBATCH %LIST%
