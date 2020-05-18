@echo off
echo ------------------------------------------
echo Fusion des images des sous dossiers en pdf
echo ------------------------------------------
echo - V2020-05-18
echo erreur normale si pas de fichier jpg dans dossier principal :
FOR /R %%G in (.) DO (
	FOR /F "delims=|" %%A IN ("%%G") DO (
		magick "%%~nxA\*.jp*g" -auto-orient -resize "2000000@>" "%%~nxA.pdf"
		echo ecriture "%%~nxA.pdf"
	)
)
