@echo off
echo ------------------------------------------
echo Fusion des images des sous dossiers en pdf
echo ------------------------------------------
echo - v2020-05-18
echo erreurs normales si pas de fichier png dans dossier :
FOR /R %%G in (.) DO (
	FOR /F "delims=|" %%A IN ("%%G") DO (
		magick "%%~nxA\*.png" -auto-orient -resize "2000000@>" "%%~nxA.pdf"
		magick "%%~nxA\*.jp*g" -auto-orient -resize "2000000@>" "%%~nxA.pdf"
		echo ecriture "%%~nxA.pdf"
	)
)
