@echo off
echo ------------------------------------------
echo Fusion des PDF d'un dossier en pdf
echo ------------------------------------------
echo -

FOR %%Z in (*.pdf) DO (
	IF NOT %%Z==merged.pdf IF NOT %%Z==merged2.pdf call mergePDF2.bat %%Z
)
