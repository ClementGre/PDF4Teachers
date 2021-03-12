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
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/bug?color=d73a4a)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.2.1%22+-label%3A%22user+Issue%22+-label%3Aduplicate+-label%3Adocumentation+-label%3Aenhancement+-label%3A%22good+first+issue%22+-label%3A%22help+wanted%22+-label%3Aquestion) 
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/user%20issue?label=user%20issues&color=36ba1b)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+label%3A%22user+issue%22+)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/enhancement?color=a2eeef)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.2.1%22+label%3A%22enhancement%22+)

# Presentation [EN]

**This app is specifically designed for teachers, it allows to annotate assessments returned in PDF with a very good productivity**

Since V1.1.0, PDF4Teachers is fully translated in English. Welcome to English speaking teachers !

PDF4Teachers is a free and open-source application.

PDF4Teachers offers tools for a productive edit of PDF files, during the annotation / correction of assessments. Edits are saved separately from PDF files, lists of previous and favorites annotations allow quick insertion of recurrent advices and corrections. Documents are managed as a set. when job is done, all documents can be exported as new PDF files with annotations on it.

PDF4Teachers includes special functionalities for annotation of grades and offers a tool for grading scale (marking scheme). When using numerical grading system, the tool computes the total and sub-totals, and exports the grades of a whole set of corrected documents as a .csv spreadsheet.

A set of pictures can be converted in PDF documents, and pages format can be edited (rotation, position…) through buttons located in the footer of each page.

PDF4Teachers is available for Windows, Linux, OSX. Go to the download section with the release button above.

Teachers speaking other languages than French and English : help us translate PDF4Teachers, check in the language settings on how to do it !

# Presentation [FR]

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisément de corriger des copies PDF.**

Elle est basée sur un système d'édition rapide des documents PDF. L'édition peut être sauvée pour reprendre le travail plus tard. Lorsque les éditions sont terminées, le document peut être exporté sous la forme d'un nouveau fichier PDF.
Les éditions sont composées de plusieurs éléments : Les commentaires (Texte), les Notes et les formes géométriques (Carrés, ronds, etc.)

Vous pouvez définir un barème pour une série de copies puis entrer les notes dans chaque copie. La somme des notes se calcule automatiquement. Une fois terminé, vous pouvez exporter les notes d'une ou plusieurs copies dans un ou plusieurs tableaux CSV (Tableaux de donnés formatable par des applications tels que LibreOffice Calc ou Excel).

Il est possible de convertir des images en documents PDF et d’éditer les pages des documents (rotation, position...) avec des boutons situés au pied de chaque page.

PDF4Teachers est conforme au RGPD de l'Union européenne : toutes les éditions et fichiers exportés sont stockés en local, les seuls accès réseaux de l'application concernent la vérification des mises à jour.

*Prévisualisation de l'interface :*

![Preview](https://raw.githubusercontent.com/clementgre/PDF4Teachers/master/images/preview.png)

# The dependencies
The application was developped with Java SE 8 (Swing), then, it moved to JavaFX with Java SE 11, Java SE 13, Java SE 14, and now, Java SE 15.

- **[JavaFx 15](https://openjfx.io/)** : The java API to create windows (GUI), and all the interfaces with the user.
- **[Apache PDF BOX 2.0.20](https://pdfbox.apache.org/)** : used to all the interactions with PDF : it generate images from PDF file, add it add the texts/images of the editing on the PDF document while exporting. **Commons Logging**, **Font BOX** and **ImageIO jpeg2000**, are dependencies of PDF Box.
- **[JMetro 11.6.11](https://pixelduke.com/java-javafx-theme-jmetro/)** : JavaFX theme. It offers you a nicer interface, including the dark mode.
- **[JLatexMath 1.0.7](https://github.com/opencollab/jlatexmath)** : used to generate images from LaTeX expressions. (LaTeX text elements feature in 1.2.0)
- **[SnakeYAML 1.26](https://bitbucket.org/asomov/snakeyaml/src/master/)** : lets read YAML files easier (editings + user datas) : it convert the YAML into ``HashMap<String, Object>`` and vice versa.
- **[Jackson Streaming API 2.10.3](https://github.com/FasterXML/jackson-core)** : lets read the JSON format. Used to send requests to GitHub to check if a new release is available.


- **[Gradle 6.3](https://gradle.org/)** is used to manage the dependencies, therefore, you can execute ``./gradlew run`` (bash) or ``gradlew.bat run`` (batch) in a command prompt to run the application using only the downloaded code and the Java JDK 15 (should be in the environment variable ``JAVA_HOME``).
- **[TranslationFileGenerator](https://github.com/clementgre/TranslationFileGenerator)** : used to generate the translations files using the code files. (Developped by me).

**[JLink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)** is used to generate an image of the code including the dependencies and the used modules of the JRE, for each platforms. (JDK 15 Tool)
**[JPackager](https://docs.oracle.com/javase/9/tools/javapackager.htm#JSWOR719)** is used to generates instalers (.deb, .msi, .dmg) for each platforms from the image that was generated by JLink. (JDK 15 Tool)

# [Code Organization](https://github.com/ClementGre/PDF4Teachers/tree/master/src/main/java/fr/clementgre/pdf4teachers)


