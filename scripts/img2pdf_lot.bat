@echo off
echo ------------------------------------------
echo Fusion des images des sous dossiers en pdf
echo ------------------------------------------
echo -
echo erreur normale si pas de fichier jpg dans dossier principal :
FOR /R %%G in (.) DO (
	FOR /F "delims=|" %%A IN ("%%G") DO (
		magick %%~nxA\*.jp*g -resize "2000000@>" %%~nxA.pdf
		echo ecriture %%~nxA.pdf
	)
)
