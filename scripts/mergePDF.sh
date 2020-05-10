#!/bin/bash
echo "fusionner tous les PDF de ce dossier, par ordre alphabétique ?"
read -p "Taper le nom du fichier de fusion, ou q pour quitter :" ans

if   [ $ans = 'Q' ] || [ $ans = 'q' ]
then
	echo "bye"
else
	"/System/Library/Automator/Combine PDF Pages.action/Contents/Resources/join.py" -o "$ans" *.pdf *.PDF
	echo terminé
fi
