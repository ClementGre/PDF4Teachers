[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red)](LICENSE)
[![Release](https://img.shields.io/github/release/themsou/PDF4Teachers.svg)](https://github.com/themsou/PDF4Teachers/releases/)

## PDF4Teachers

**This app is specifically designed for teachers, it allows to annotate assessments returned in PDF with a very good productivity**

Since V1.1.0, PDF4Teachers is fully translated in English. Welcome to English speaking teachers !

PDF4Teachers is a free and open-source application.

PDF4Teachers offers tools for a productive edit of PDF files, during the annotation / correction of assessments. Edits are saved separately from PDF files, lists of previous and favorites annotations allow quick insertion of recurrent advices and corrections. Documents are managed as a set. when job is done, all documents can be exported as new PDF files with annotations on it.

PDF4Teachers includes special functionalities for annotation of grades and offers a tool for grading scale (marking scheme). When using numerical grading system, the tool computes the total and sub-totals, and exports the grades of a whole set of corrected documents as a .csv spreadsheet.

PDF4Teachers is available for Windows, Linux, OSX. Go to the download section with the release button above.

Teachers speaking other languages than French and English : help us translate PDF4Teachers, check in the language settings on how to do it !

---------------------------------------------------------------------------------------------------

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger des copies PDF.**

Elle est basée sur un système d'édition rapide des documents PDF. L'édition peut être sauvée pour reprendre le travail plus tard. Lorsque les éditions sont terminées, le document peut être exporté sous la forme d'un nouveau fichier PDF.
Les éditions sont composées de plusieurs éléments : Les commentaires (Texte), les Notes et les formes géométriques (Carrés, ronds, etc.)

Vous pouvez définir un barème pour une série de copies puis entrer les notes dans chaque copie. La somme des notes se calcule automatiquement. Une fois terminé, vous pouvez exporter les notes d'une ou plusieurs copies dans un ou plusieurs fichiers CSV (Tableaux de donnés formatable par LibreOffice Calc ou Excel).

*Prévisualisation de l'interface :*

![Preview](https://raw.githubusercontent.com/themsou/PDF4Teachers/master/preview.png)

## Les APIs

L'application a été développée sous Java SE 8 (avec Swing) puis elle est passé sous JavaFX avec Java SE 11, Java SE 13 et enfin, Java SE 14.

- J'utilise l'API PDF BOX pour générer des images à partir d'un fichier PDF puis pour régénérer un fichier PDF, ainsi que commons-logging et Font BOX qui lui sont nécessaires.
- L'application, initialement basé sur Swing, a migré vers JavaFx pour bénéficier de tous ses avantages (Plus récent / encore maintenus, Bindings, etc.).
- L'API JMetro, est un thème JavaFX qui m'a permis de vous offrir une interface plus confortable à regarder que celle par défaut de JavaFx.
- Jackson Streaming API permet d'interpréder des donnés Json, il me permet de faire des requêtes à GitHub pour vérifier si une nouvelle version est disponible.
- J'ai choisi Gradle pour gérer les dépendances, vous pouvez donc exécuter ``./gradlew run`` en bash ou ``gradlew.bat run`` en batch dans un terminal de commande pour exécuter l'application. Vous devrez bien sur auparavant avoir installé un JDK 13 et avoir donné son chemin d'accès dans votre variable d'environment ``JAVA_HOME``.

Vous retrouverez aussi dans l'onglet release des versions compilés avec JLink et JPackager pour votre système d'exploitation.

## L'organisation du code

*Les noms de packages commençants par un ``.`` représentent des packages de ``fr.themsou``*

La classe main se situe dans le package ``fr.themsou.main``

**Démmarage de l'application**

Au démarrage de l'application, ``Main`` va vérifier si une langue est définie, si non, elle va ouvrir la fenêtre de choix de langage (``.windows.LanguageWindow``). Elle va ensuite ouvir la fenêtre de validation de liscence (``.windows.LicenceWindow``) si l'application est à son premier démarrage (Détecté avec la présence du fichier ``settings.yml``).
Enfin, Main va appeler (``.windows.MainWindow``) qui va tout initialiser et préparer l'interface principale.
(Toutes les classes de ``fr.themsou.windows`` etendent de ``Stage``, qui représente une fenêtre dans JavaFx).

**Classes des éléments graphiques de JavaFX (``fr.themsou.panel``)**

Chaque classe ou package du package ``fr.themsou.panel`` et ``fr.themsou.panel.leftBar`` représentent un élément graphique de l'écran, elle etendent indirectement de ``javafx.scene.Node``, (JPanel en Swing).

On y retrouve donc FooterBar (La barre d'état en bas), MenuBar (Le Menu en haut), MainScreen (là où s'affichera le document à éditer) et quelques classes du package LeftBar qui sont les différents ``Tab`` du ``TabPane`` initialisé dans MainWindow (``LBFilesTab``, ``LBTextTab``, ``LBNoteTab``, ``LBPaintTab``). Ces classes sont accompagnés d'autres classes dont les ``xxxTreeView`` ou ``xxxTreeView`` qui représentent un arbre ou une liste JavaFX. On retrouve aussi les ``xxxTreeItem`` qui représentent un élément de l'arbre. Ces classes ont généralement une variable ``core``, qui représentent l'élément de ``.document.editions.elements`` qui leurs correspond.

**Classes des éléments (``fr.themsou.document.editions.elements``)**

Ces différents éléments (texte, notes et formes géométriques) ont des classes attribués dans ``.document.editions.elements`` qui implémentent ``Element``, étendent d'un élément graphique de ``javafx.scene.control`` et qui permettent de stoquer les donnés d'un élément et d'encoder/décoder les donnés en hexadécimal. Ces classes gèrent tout ce qui est relatif à l'élément en question.

**Classes pour gérer les documents PDF (``fr.themsou.document``)**

À l'ouverture d'un document, ``fr.themsou.panel.MainScreen.MainScreen`` initialisera :
- ``.document.Document`` qui initialisera sous demande de MainScreen :

  - ``.document.editions.Edition`` qui chargera l'édition du document depuis un fichier écrit en Hexadécimal et stoqué dans ``<user.home>/.PDF4Teachers/<nom de l'édition>.edit`` sous Mac et Linux et dans ``<AppData/Romaning>/PDF4Teachers/<nom de l'édition>.edit``. Il traduira l'Hexadécimal en classes du package (``.document.edition.elements``) et les ajoutera aux instances de PageRenderer enregistrés dans ``Document``. Il pourra aussi écrire les fichiers lors de la sauvegarde.
  
  - ``.document.render.PageRenderer`` pour chacune des pages en passant en paramètre le numéro de la page correspondant. PageRenderer stoquera touts les Elements dans une ``ArrayList<Element>``. Chaque ``PageRenderer`` fera un rendu sous forme d'image de la page du fichier PDF qui lui est assigné, avec ``.document.render.PDFPagesRender``. Le rendu se lancera lorsque la page sera proche de la zone visible de la ScrollPane.

