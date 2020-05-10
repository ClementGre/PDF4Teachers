#!/bin/bash
echo "fusionner tous les PDF de ce dossier, par ordre alphabétique ?"
read -p "Taper le nom du fichier de fusion, ou q pour quitter :" ans

if   [ $ans = 'Q' ] || [ $ans = 'q' ]
then
	echo "bye"
else
	FILES='ls *.pdf'
	LIST=${FILES:2}
	gs -dNOPAUSE -sDEVICE=pdfwrite -sOUTPUTFILE=$ans -dBATCH $LIST
fi
