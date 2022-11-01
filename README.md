[![Java CI with Gradle](https://github.com/clementgre/PDF4Teachers/workflows/build/badge.svg)](https://github.com/clementgre/PDF4Teachers/actions?query=workflow%3Abuild)
[![TotalDownloads](https://img.shields.io/github/downloads/clementgre/PDF4Teachers/total)](https://github.com/clementgre/PDF4Teachers/releases/latest)
[![LatestDownloads](https://img.shields.io/github/downloads/clementgre/PDF4Teachers/latest/total)](https://github.com/clementgre/PDF4Teachers/releases/latest)
[![Commit activity](https://img.shields.io/github/commit-activity/m/clementgre/pdf4teachers)](https://github.com/ClementGre/PDF4Teachers/commits/master)
[![Commit since latest](https://img.shields.io/github/commits-since/clementgre/pdf4teachers/latest)](https://github.com/ClementGre/PDF4Teachers/commits/master)
[![Contributors](https://img.shields.io/github/contributors/clementgre/pdf4teachers)](https://github.com/ClementGre/PDF4Teachers/graphs/contributors)
<br/>
[![Star](https://img.shields.io/github/stars/clementgre/PDF4Teachers?label=Star%20PDF4Teachers&style=social)](https://github.com/clementgre/PDF4Teachers)
[![GitHubFolowers](https://img.shields.io/github/followers/clementgre?label=Follow%20Clément%20Grennerat&style=social)](https://github.com/clementgre)
[![TwitterFolowers](https://img.shields.io/twitter/follow/Pdf4Teachers?style=social)](https://twitter.com/Pdf4Teachers)


<h3 align="center">
  <img src="https://raw.githubusercontent.com/ClementGre/PDF4Teachers/master/src/main/resources/logo.png" alt="Logo" width="120" height="120"><br>
  PDF4Teachers<br>
  <a href="https://pdf4teachers.org">https://pdf4teachers.org</a>
</h3>
<p align="center">
  <a href="#presentation-en">Presentation</a> | <a href="#the-dependencies">The dependencies</a> | <a href="https://github.com/ClementGre/PDF4Teachers/tree/master/src/main/java/fr/clementgre/pdf4teachers">Code organization</a><br/>
  PDF editing software in large quantities designed for teachers.<br/><br/>
  <img src="https://raw.githubusercontent.com/ClementGre/PDF4Teachers/master/images/banner-flat.png" alt="Logo" width="690"/><br/>
</p>

#### Shortcuts : &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Before opening new issue, see :

[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red?label=Read%20license)](LICENSE)
[![Release](https://img.shields.io/github/v/release/clementgre/PDF4Teachers?label=Download%20version)](https://github.com/clementgre/PDF4Teachers/releases/latest)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/bug?color=d73a4a)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.4.0%22+-label%3A%22user+Issue%22+-label%3Aduplicate+-label%3Adocumentation+-label%3Aenhancement+-label%3A%22good+first+issue%22+-label%3A%22help+wanted%22+-label%3Aquestion)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/user%20issue?label=user%20issues&color=36ba1b)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+label%3A%22user+issue%22+)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/enhancement?color=a2eeef)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.4.0%22+label%3A%22enhancement%22+)

# Presentation [EN]

**This app is specifically designed for teachers, it allows annotating assessments returned in PDF with a very good
productivity**

Since V1.1.0, PDF4Teachers is fully translated in English. Welcome to English-speaking teachers !

PDF4Teachers is a free and open-source application.

PDF4Teachers offers tools for a productive edit of PDF files, during the annotation / correction of assessments. Edits
are saved separately from PDF files, lists of previous and favorites annotations allow quick insertion of recurrent
advices and corrections. Documents are managed as a set. when job is done, all documents can be exported as new PDF
files with annotations on it.

PDF4Teachers includes special functionalities for annotation of grades and offers a tool for grading scale (marking
scheme). When using numerical grading system, the tool computes the total and sub-totals, and exports the grades of a
whole set of corrected documents as a .csv spreadsheet.

A set of pictures can be converted in PDF documents, and pages format can be edited (rotation, position…) through
buttons located in the footer of each page.

PDF4Teachers is available for Windows, Linux, OSX. Go to the download section with the release button above.

Teachers speaking other languages than French and English : help us translate PDF4Teachers, check in the language
settings on how to do it !

# Presentation [FR]

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger
des copies PDF.**

Elle est basée sur un système d'édition rapide des documents PDF. L'édition peut être sauvée pour reprendre le travail
plus tard. Lorsque les éditions sont terminées, le document peut être exporté sous la forme d'un nouveau fichier PDF.
Les éditions sont composées de plusieurs éléments : Les commentaires (Texte), les Notes et les formes géométriques (
Carrés, ronds, etc.)

Vous pouvez définir un barème pour une série de copies puis entrer les notes dans chaque copie. La somme des notes se
calcule automatiquement. Une fois terminé, vous pouvez exporter les notes d'une ou plusieurs copies dans un ou plusieurs
tableaux CSV (Tableaux de donnés formatable par des applications tels que LibreOffice Calc ou Excel).

Il est possible de convertir des images en documents PDF et d’éditer les pages des documents (rotation, position...)
avec des boutons situés au pied de chaque page.

PDF4Teachers est conforme au RGPD de l'Union européenne : toutes les éditions et fichiers exportés sont stockés en
local, les seuls accès réseaux de l'application concernent la vérification des mises à jour.

*Prévisualisation de l'interface :*

![Preview](https://raw.githubusercontent.com/clementgre/PDF4Teachers/master/images/preview.png)

# The dependencies

The application was developed with JavaFX framework, in Java SE 17 (1.4.0).

### Java dependencies

- **[JavaFX 17](https://openjfx.io/)** : The java API to create windows (GUI), and all the interfaces with the user.
- **[Apache PDF BOX 2.0.26](https://pdfbox.apache.org/)** : used to all the interactions with PDF : it generates images
  from PDF file, add it add the texts/images of the editing on the PDF document while exporting. **Commons Logging**, **
  Font BOX**, **JAI ImageIO** and **jbig2-imageio**, are dependencies of PDF Box.
- **[JMetro 11.6.15](https://pixelduke.com/java-javafx-theme-jmetro/)** : JavaFX theme. It offers you a nicer interface,
  including the dark mode.
- **[ControlsFX 11.1.0](https://controlsfx.github.io/)** : JavaFX new inputs and custom panes
- **[Writer2Latex 1.6.1](http://writer2latex.sourceforge.net/)** : used to convert StarMath (or LibreOffice Math)
  language in LaTeX for rendering.
  **[JLatexMath 1.0.7](https://github.com/opencollab/jlatexmath)** : used to generate images from LaTeX expressions. (
  LaTeX text elements feature in 1.2.0)
- **[SnakeYAML 1.30](https://bitbucket.org/asomov/snakeyaml/src/master/)** : lets read YAML files easier (edits + user
  datas) : it converts the YAML into ``HashMap<String, Object>`` and vice versa.
- **[Opencsv 5.6](http://opencsv.sourceforge.net/)** : used to read/write CSV files (when importing SACoche assessment)
- **[Jackson Streaming API 2.13.3](https://github.com/FasterXML/jackson-core)** : lets read the JSON format. Used to
  send requests to GitHub to check if a new release is available.
- **[Metadata-Extractor 2.18.0](https://drewnoakes.com/code/exif/)** : Used to read the images EXIF data, so
  PDF4Teachers can take in account the rotation of images
- **[Batik Parser 1.14](https://xmlgraphics.apache.org/batik/using/parsers.html)** : Allow me to parse SVG easily, with
  custom handlers.
- **[PdfBox Graphics2D 0.40](https://github.com/rototor/pdfbox-graphics2d)** : Used to write SVG to a PDF, using PDFBox.
- **[Google Diff-Match-Patch 0.1](https://github.com/google/diff-match-patch)** : Allow me to perform some actions on
  strings..
- **[Unique4J 1.4](https://github.com/prat-man/unique4j)** : used to set up a single instance of the app when opening files
  from system.
- **[jSystemThemeDetector 3.8](https://github.com/Dansoftowner/jSystemThemeDetector)** : used to set up a single instance of the app when opening files
  from system.

### Gradle plugins

The dependencies of the application are managed by [Gradle](https://gradle.org/) 7 (rc-2), therefore, you can
execute ``./gradlew run`` (bash) or ``gradlew.bat run`` (batch) in a command prompt to run the application using only
the downloaded code, and the Java JDK 16 (should be in the environment variable ``JAVA_HOME``). Gradle is using some
plugins to manage dependencies :

- **[Badass JLink Plugin](https://github.com/beryx/badass-jlink-plugin/)** allow me to use JLink and JPackager (
  distribution tools) directly with gradle.
- **[JavaFX Gradle 7 Plugin](https://github.com/xzel23/javafx-gradle-plugin)** fork of the original JavaFX Gradle
  Plugin, allows to setup the JavaFX libs with Gradle 7 and JDK 16+
- **[Extra Java Module Info](https://github.com/jjohannes/extra-java-module-info)** lets us customize module-info of any
  jar file, and then, it allows me to use non modular dependencies (Allows me to give a name to any "unnamed module").

### Software used

- **[i18nDotPropertiesGUI](https://github.com/ClementGre/i18nDotPropertiesGUI)** : is used to manage the translations
  files, and to fill them.

### Packaging tool

- **[JLink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)** is a
  distribution tool used to generate an image of the code including the dependencies and the used modules of the JRE,
  for each platform (exports all the code and the JRE so there is no need to have Java installed to run PDF4Teachers). (JDK tool)
- **[JPackage](https://docs.oracle.com/en/java/javase/14/docs/specs/man/jpackage.html)** is used to generate Windows .msi and
  Linux .deb installers for each platform from the image that was generated by JLink. (JDK tool)
- **[DMGCanvas](https://www.araelium.com/dmgcanvas)** allows me to create a custom OSX DMG file, from the .app created
  by hand from JLink image. Needs to be installed to use the ``gradlew autoPackage`` task on Mac OSX.
- **[WiX Toolset](https://wixtoolset.org/)** is a JPackage dependency, it allows to build .msi file. Needs to be installed to use the ``gradlew autoPackage`` task on Windows.

The installer can be build automatically using the custom ``autoPackage`` task on any platform.

### Fonts for the documentations files

If you have to work on a documentation translation, it is always better to have the right fonts installed to have the correct layout (and if you eventually have to generate the PDF from it).
Then you will need these fonts :
- Arial Rounded MT Bold
- Calibri
- Liberation Sans

# [Code Organization](https://github.com/ClementGre/PDF4Teachers/tree/master/src/main/java/fr/clementgre/pdf4teachers)


