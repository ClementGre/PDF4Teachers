[![Java CI with Gradle](https://github.com/clementgre/PDF4Teachers/workflows/build/badge.svg)](https://github.com/clementgre/PDF4Teachers/actions?query=workflow%3Abuild)
[![TotalDownloads](https://img.shields.io/github/downloads/clementgre/PDF4Teachers/total)](https://github.com/clementgre/PDF4Teachers/releases/latest)
[![LatestDownloads](https://img.shields.io/github/downloads/clementgre/PDF4Teachers/latest/total)](https://github.com/clementgre/PDF4Teachers/releases/latest)
[![Star](https://img.shields.io/github/stars/clementgre/PDF4Teachers?label=Star%20PDF4Teachers&style=social)](https://github.com/clementgre/PDF4Teachers)
[![GitHubFolowers](https://img.shields.io/github/followers/clementgre?label=Follow%20Clément%20Grennerat&style=social)](https://github.com/clementgre)
[![TwitterFolowers](https://img.shields.io/twitter/follow/Pdf4Teachers?style=social)](https://twitter.com/Pdf4Teachers)

![Preview](https://raw.githubusercontent.com/clementgre/PDF4Teachers/master/images/banner-flat.png)

##### Shortcuts : &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Before opening new issue, see :

[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red?label=Read%20license)](LICENSE)
[![Release](https://img.shields.io/github/v/release/clementgre/PDF4Teachers?label=Download%20version)](https://github.com/clementgre/PDF4Teachers/releases/latest)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/bug?color=d73a4a)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.2.0%22+-label%3A%22user+Issue%22+-label%3Aduplicate+-label%3Adocumentation+-label%3Aenhancement+-label%3A%22good+first+issue%22+-label%3A%22help+wanted%22+-label%3Aquestion) 
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/user%20issue?label=user%20issues&color=36ba1b)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+label%3A%22user+issue%22+)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/enhancement?color=a2eeef)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.2.0%22+label%3A%22enhancement%22+)

## PDF4Teachers [EN]

**This app is specifically designed for teachers, it allows to annotate assessments returned in PDF with a very good productivity**

Since V1.1.0, PDF4Teachers is fully translated in English. Welcome to English speaking teachers !

PDF4Teachers is a free and open-source application.

PDF4Teachers offers tools for a productive edit of PDF files, during the annotation / correction of assessments. Edits are saved separately from PDF files, lists of previous and favorites annotations allow quick insertion of recurrent advices and corrections. Documents are managed as a set. when job is done, all documents can be exported as new PDF files with annotations on it.

PDF4Teachers includes special functionalities for annotation of grades and offers a tool for grading scale (marking scheme). When using numerical grading system, the tool computes the total and sub-totals, and exports the grades of a whole set of corrected documents as a .csv spreadsheet.

A set of pictures can be converted in PDF documents, and pages format can be edited (rotation, position…) through buttons located in the footer of each page.

PDF4Teachers is available for Windows, Linux, OSX. Go to the download section with the release button above.

Teachers speaking other languages than French and English : help us translate PDF4Teachers, check in the language settings on how to do it !

## PDF4Teachers [FR]

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger des copies PDF.**

Elle est basée sur un système d'édition rapide des documents PDF. L'édition peut être sauvée pour reprendre le travail plus tard. Lorsque les éditions sont terminées, le document peut être exporté sous la forme d'un nouveau fichier PDF.
Les éditions sont composées de plusieurs éléments : Les commentaires (Texte), les Notes et les formes géométriques (Carrés, ronds, etc.)

Vous pouvez définir un barème pour une série de copies puis entrer les notes dans chaque copie. La somme des notes se calcule automatiquement. Une fois terminé, vous pouvez exporter les notes d'une ou plusieurs copies dans un ou plusieurs tableaux CSV (Tableaux de donnés formatable par des applications tels que LibreOffice Calc ou Excel).

Il est possible de convertir des images en documents PDF et d’éditer les pages des documents (rotation, position...) avec des boutons situés au pied de chaque page.

PDF4Teachers est conforme à la RGPD française : toutes les éditions et fichiers exportés sont stockés en local, les seuls accès réseaux de l'application concernent la vérification des mises à jour.

*Prévisualisation de l'interface :*

![Preview](https://raw.githubusercontent.com/clementgre/PDF4Teachers/master/images/preview.png)

## Les APIs

L'application a été développée sous Java SE 8 (avec Swing) puis elle est passé sous JavaFX avec Java SE 11, Java SE 13 et enfin, Java SE 14.

- L'application, initialement basé sur Swing, a migré vers **[JavaFx 14](https://openjfx.io/)** pour bénéficier de tous ses avantages (Plus récent / encore maintenus, Bindings, etc.).
- **[Apache PDF BOX 2.0.20](https://pdfbox.apache.org/)** est utilisé pour générer des images à partir d'un fichier PDF puis pour régénérer un nouveau fichier PDF avec divers éléments (exportation). **Commons Logging**, **Font BOX** et **ImageIO jpeg2000**, lui sont nécessaires.
- **[JMetro 11.6.11](https://pixelduke.com/java-javafx-theme-jmetro/)**, est un thème JavaFX qui m'a permis de vous offrir une interface plus confortable à regarder que celle par défaut de JavaFx.
- **[JLatexMath 1.0.7](https://github.com/opencollab/jlatexmath)** permet de générer des images à partir de commandes LaTeX (Possibilité de faire des éléments textuels écrits en LaTeX).
- **[SnakeYAML 1.26](https://bitbucket.org/asomov/snakeyaml/src/master/)** permet de lire et d'écrire les fichiers .yml (éditions et donnés utilisateurs) plus facilement : il convertis l'YAML en ``HashMap<String, Object>`` et inversement.
- **[Jackson Streaming API 2.10.3](https://github.com/FasterXML/jackson-core)** permet d'interpréder des donnés Json, il me permet de faire des requêtes à GitHub pour vérifier si une nouvelle version est disponible.


- J'ai choisi **[Gradle 6.3](https://gradle.org/)** pour gérer les dépendances, vous pouvez donc exécuter ``./gradlew run`` en bash ou ``gradlew.bat run`` en batch dans un terminal de commande pour exécuter l'application. Vous devrez bien sur auparavant avoir installé un JDK 14 et avoir donné son chemin d'accès dans votre variable d'environment ``JAVA_HOME``.
- Les fichiers de traductions ont été générés grace à **[TranslationFileGenerator](https://github.com/clementgre/TranslationFileGenerator)**

Vous retrouverez aussi dans l'onglet release des versions compilés avec JLink et JPackager pour votre système d'exploitation.

## L'organisation du code (1.2.0)

*Les noms de packages commençants par un ``.`` représentent des packages de ``fr.themsou``*

La classe main se situe dans le package ``fr.themsou.main``

**Démmarage de l'application**

Au démarrage de l'application, ``Main`` va vérifier si une langue est définie, si non, elle va ouvrir la fenêtre de choix de langage (``.windows.LanguageWindow``). Elle va ensuite ouvir la fenêtre de validation de liscence (``.windows.LicenceWindow``) si l'application est à son premier démarrage (Détecté avec la présence du fichier ``settings.yml``).
Enfin, Main va appeler (``.windows.MainWindow``) qui va tout initialiser et préparer l'interface principale.
(Toutes les classes de ``fr.themsou.windows`` étendent de ``Stage``, qui représente une fenêtre dans JavaFx).

**Classes des éléments graphiques de JavaFX (``fr.themsou.panel``)**

Chaque classe ou package du package ``fr.themsou.panel`` et ``fr.themsou.panel.leftBar`` représentent un élément graphique de l'écran, elle étendent indirectement de ``javafx.scene.Node``, (JPanel en Swing).

On y retrouve donc FooterBar (La barre d'état en bas), MenuBar (Le Menu en haut), MainScreen (là où s'affichera le document à éditer) et quelques classes du package LeftBar qui sont les différents ``Tab`` du ``TabPane`` initialisé dans MainWindow (``LBFilesTab``, ``LBTextTab``, ``LBGradeTab``, ``LBPaintTab``). Ces classes sont accompagnés d'autres classes dont les ``xxxTreeView`` ou ``xxxListView`` qui représentent un arbre ou une liste JavaFX. On retrouve aussi les ``xxxTreeItem`` ou ``xxxListItem`` qui représentent un élément de l'arbre (ou de la liste). Ces classes ont généralement une variable ``core``, qui représente l'élément de ``.document.editions.elements`` qui lui correspond.

**Classes des éléments (``fr.themsou.document.editions.elements``)**

Ces différents éléments (texte, notes et formes géométriques) ont des classes attribués dans ``.document.editions.elements`` qui étendent ``Element``. ``Element`` étend de ``Region`` (Conteneur JavaFX). Ces différentes classes qui étendent ``Element`` contiennent un composant JavaFx, qui sera le "children" de la ``Region`` que représente la classe. ``Element`` s'occupe de toutes les fonctionnalités communes (coordonnés, interactions...).

**Classes pour gérer les documents PDF (``fr.themsou.document``)**

À l'ouverture d'un document, ``fr.themsou.panel.MainScreen.MainScreen`` initialisera :
- ``.document.Document`` qui initialisera sous demande de MainScreen :

  - ``.document.editions.Edition`` qui chargera l'édition du document depuis un fichier écrit en YAML et stocké dans ``<Dossier Utilisateur>/.PDF4Teachers/<nom de l'édition>.yml`` sous Mac et Linux et dans ``<AppData/Romaning>/PDF4Teachers/<nom de l'édition>.yml`` sous Windows. Il traduira l'Hexadécimal en classes du package (``.document.edition.elements``) et les ajoutera aux instances de PageRenderer enregistrés dans ``Document``. Il pourra aussi écrire les fichiers lors de la sauvegarde.
  
  - ``.document.render.PageRenderer`` pour chacune des pages en passant en paramètre le numéro de la page correspondant. PageRenderer stockera tous les Elements dans une ``ArrayList<Element>``. Chaque ``PageRenderer`` fera un rendu sous forme d'image de la page du fichier PDF qui lui est assigné, avec ``.document.render.PDFPagesRender``. Le rendu se lancera lorsque la page sera proche de la zone visible de la ScrollPane et se re-lancera si le niveau de zoom change.

## Statistiques :

![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-Windows-1.2.0.msi)
![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-Windows-1.2.0-BIN.zip)

![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-MacOSX-1.2.0.dmg)
![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-MacOSX-1.2.0-BIN.zip)

![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-Linux-1.2.0.deb)
![](https://img.shields.io/github/downloads-pre/clementgre/PDF4Teachers/1.2.0/PDF4Teachers-Linux-1.2.0-BIN.tar.gz)