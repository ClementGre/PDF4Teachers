[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red)](LICENSE)
[![Build Status](https://travis-ci.com/themsou/PDFTeacher.svg?branch=master)](https://travis-ci.com/themsou/PDFTeacher/)
[![Release](https://img.shields.io/github/release/themsou/PDFTeacher.svg)](https://github.com/themsou/PDFTeacher/releases/)

## PDFTeacher <En cours de développement>

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger des copies PDF.**

Elle est basée sur un système d'édition, vous pouvez éditer votre document et sauvegarder l'édition pour reprendre votre travil plus tard. Vous pouvez ensuite exporter le document sous la forme d'un nouveau fichier PDF.
Les éditions sont composés de plusieurs éléments : Les commentaires (Texte), les Notes* et les formes géométriques (Carrés, ronds etc.)

*La somme des notes se calculera automatiquement et elles pouront être exportés dans un tableau CSV.

## Les APIs

L'application à été développé sous Java SE 11.

- J'utilise l'API PDF BOX pour générer des images à partir d'un fichier PDF ainsi que commons-logging et Font BOX qui lui sont nécessaires.
- L'application, initialement basé sur Swing, a migré vers JavaFx pour bénéficier de tous ses avantages (Plus récent / encore maintenus, Bindings etc.).
- J'ai choisit Gradle pour gérer les dépendances, vous pouvez donc éxécuter ``./gradlew run`` dans un terminal de commande pour exécuter l'application. 

Des releases seront aussi bientôt disponibles.

## L'organisation du code

*Les noms de packages commençants par un ``.`` représentent des packages de ``fr.themsou``*

La classe main se situe dans le package ``fr.themsou.main``
Les classes du package ``fr.themsou.panel`` représentent toutes une partie de l'écran, elle etendent indirectement de ``javafx.scene.Node``, (JPanel en Swing).

On y retrouve donc FooterBar (La barre d'état en bas), MenuBar (Le Menu en haut), MainScreen (La où s'affichera le document à éditer) et toutes les classes du package LeftBar qui sont les différents ``Tab`` du ``TabPane`` initialisé dans Main. Les différents onglets permettrons respectivement de voir la liste des fichiers, ajouter du texte, des notes et des formes géométriques.

**Classes pour gérer l'édition du document (``fr.themsou.panel.LeftBar``)**

Ces différents éléments (texte, notes etc.) ont des classes attribués dans ``.document.editions.elements`` qui implémenterons ``Element``, etendront d'un élément graphique de ``javafx.scene.control`` et qui permettront de faire le rendu, de stoquer les donnés d'un élément et d'encoder/décoder les donnés en hexadécimal.

**Classes pour gérer les documents PDF (``fr.themsou.document``)**

À l'ouverture d'un document, ``fr.themsou.panel.MainScreen`` initialisera :

- ``.document.Document`` qui fera le rendu des pages avec ``.document.render.PDFPagesRender``, il initialisera ensuite sous demande de MainScreen :
  - ``.document.render.PageRenderer`` pour chacune des pages en passant en paramètre une image : le rendu de la page. PageRenderer stoquera touts les Elements dans une ArrayList<>. Document et PageRenderer feront les Bindings nécessaires pour automatiser la taille de la Page par rapport au zoom et aux dimensions de MainScreen.
  - ``.document.editions.Edition`` qui chargera l'édition du document depuis un fichier écrit en Hexadécimal et stoqué dans ``<user.home>/.PDFTEacher/<nom de l'édition>.edit``. Il traduira l'Hexadécimal en Classes avec les classes des éléments (``.document.edition.elements``) et les ajoutera aux instances de PageRenderer enregistrés dans Document. Il poura aussi écrire les fichiers lors de la sauvegarde.
