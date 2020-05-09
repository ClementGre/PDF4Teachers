@echo off
echo ------------------------------------------
echo Fusion des images des sous dossiers en pdf
echo ------------------------------------------
echo -
echo erreurs normales si pas de fichier png dans dossier :
FOR /R %%G in (.) DO (
	FOR /F "delims=|" %%A IN ("%%G") DO (
		magick "%%~nxA\*.png" -resize "2000000@>" %%~nxA.pdf
		echo ecriture %%~nxA.pdf
	)
)
