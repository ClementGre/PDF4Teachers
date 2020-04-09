[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red)](LICENSE)
[![Release](https://img.shields.io/github/release/themsou/PDF4Teachers.svg)](https://github.com/themsou/PDF4Teachers/releases/)

## PDF4Teachers

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger des copies PDF.**

Elle est basée sur un système d'édition, vous pouvez éditer votre document et sauvegarder l'édition pour reprendre votre travil plus tard. Vous pouvez ensuite exporter le document sous la forme d'un nouveau fichier PDF.
Les éditions sont composés de plusieurs éléments : Les commentaires (Texte), les Notes* et les formes géométriques (Carrés, ronds etc.)

*La somme des notes se calculera automatiquement et elles pouront être exportés dans un tableau CSV.

*Prévisualisation de l'interface :*

![Preview](https://raw.githubusercontent.com/themsou/PDF4Teachers/master/preview.png)

## Les APIs

L'application à été développé sous Java SE 8 (avec Swing) puis passée sous Java SE 11 avec JavaFX et enfin sous Java SE 13.

- J'utilise l'API PDF BOX pour générer des images à partir d'un fichier PDF puis pour regénérer un fichier PDF, ainsi que commons-logging et Font BOX qui lui sont nécessaires.
- L'application, initialement basé sur Swing, a migré vers JavaFx pour bénéficier de tous ses avantages (Plus récent / encore maintenus, Bindings etc.).
- J'ai choisi Gradle pour gérer les dépendances, vous pouvez donc éxécuter ``./gradlew run`` en bash ou ``gradlew.bat run`` en batch dans un terminal de commande pour exécuter l'application. 

Vous retrouverez aussi dans l'onglet release des versions compilés avec JLink pour votre système d'exploitation.

## L'organisation du code

*Les noms de packages commençants par un ``.`` représentent des packages de ``fr.themsou``*

La classe main se situe dans le package ``fr.themsou.main``

**Classes des éléments graphiques de JavaFX (``fr.themsou.panel``)**

Les classes du package ``fr.themsou.panel`` représentent toutes une partie de l'écran, elle etendent indirectement de ``javafx.scene.Node``, (JPanel en Swing).

On y retrouve donc FooterBar (La barre d'état en bas), MenuBar (Le Menu en haut), MainScreen (là où s'affichera le document à éditer) et toutes les classes du package LeftBar qui sont les différents ``Tab`` du ``TabPane`` initialisé dans Main. Les différents onglets permettrons respectivement de voir la liste des fichiers, ajouter du texte, des notes et des formes géométriques.

**Classes des éléments (``fr.themsou.document.editions.elements``)**

Ces différents éléments (texte, notes et formes géométriques) ont des classes attribués dans ``.document.editions.elements`` qui implémenterons ``Element``, etendront d'un élément graphique de ``javafx.scene.control`` et qui permettront de faire le rendu, de stoquer les donnés d'un élément et d'encoder/décoder les donnés en hexadécimal.

**Classes pour gérer les documents PDF (``fr.themsou.document``)**

À l'ouverture d'un document, ``fr.themsou.panel.MainScreen`` initialisera :
- ``.document.Document`` qui initialisera sous demande de MainScreen :

  - ``.document.editions.Edition`` qui chargera l'édition du document depuis un fichier écrit en Hexadécimal et stoqué dans ``<user.home>/.PDF4Teachers/<nom de l'édition>.edit`` sous Mac et Linux et dans ``<AppData/Romaning>/PDF4Teachers/<nom de l'édition>.edit``. Il traduira l'Hexadécimal en classes du package (``.document.edition.elements``) et les ajoutera aux instances de PageRenderer enregistrés dans ``Document``. Il poura aussi écrire les fichiers lors de la sauvegarde.
  
  - ``.document.render.PageRenderer`` pour chacune des pages en passant en paramètre le numéro de la page correspondant. PageRenderer stoquera touts les Elements dans une ``ArrayList<>``. ``MainScreen`` et ``PageRenderer`` feront les Bindings nécessaires pour automatiser la taille de la Page par rapport au zoom et aux dimensions de ``MainScreen``. Chaque ``PageRenderer`` fera un rendu sous forme d'image de la page du fichier PDF qui lui est assigné, avec ``.document.render.PDFPagesRender``. Le rendu se lancera lorsque la page sera proche de la zone visible de la ScrollPane.

