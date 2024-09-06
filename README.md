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
  <a href="#presentation">Presentation</a> | <a href="#features">Features</a> | <a href="#dependencies">Dependencies</a> | <a href="#code-organization">Code organization</a><br/>
  PDF editing software in large quantities designed for teachers.<br/><br/>
  <img src="https://raw.githubusercontent.com/ClementGre/PDF4Teachers/master/images/banner-flat.png" alt="Logo" width="690"/><br/>
</p>

#### Shortcuts : &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Before opening new issue, see :

[![License](https://img.shields.io/badge/Licence-Apache%20Licence%202.0-red?label=Read%20license)](LICENSE)
[![Release](https://img.shields.io/github/v/release/clementgre/PDF4Teachers?label=Download%20version)](https://github.com/clementgre/PDF4Teachers/releases/latest)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/bug?color=d73a4a)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.4.1%22+-label%3A%22user+Issue%22+-label%3Aduplicate+-label%3Adocumentation+-label%3Aenhancement+-label%3A%22good+first+issue%22+-label%3A%22help+wanted%22+-label%3Aquestion)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/user%20issue?label=user%20issues&color=36ba1b)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+label%3A%22user+issue%22+)
[![GitHub labels](https://img.shields.io/github/issues/clementgre/PDF4Teachers/enhancement?color=a2eeef)](https://github.com/clementgre/PDF4Teachers/issues?q=is%3Aissue+milestone%3A%22Release+1.4.1%22+label%3A%22enhancement%22+)

# Presentation

**PDF4Teachers is specifically designed for teachers. It allows you to annotate assessments returned in PDF with
excellent productivity.**

PDF4Teachers provides tools for editing PDF files productively while annotating/correcting assessments. It memorizes all
previously added annotations and offers quick insertion of them. PDF4Teachers also supports the definition of a grade
scale (numerical or competency-based grading). Grades can then be exported as a .csv spreadsheet.

PDF4Teachers can also perform batch conversion of images to PDF to convert multiple documents at the same time. A lot of
other PDF tools are also available, including booklet assembler/disassembler, PDF merge and split, move and rotate
pages, and more.

PDF4Teachers supports freeform drawing, vector SVG path elements, images, LaTeX and LibreOffice Math equations.

PDF4Teachers saves any edits made to a PDF file as a separate layer, so the original PDF remains intact (except for any
changes made with PDF tools). You can export the edited document as a new PDF file within the app.

## Preview

![Preview](https://raw.githubusercontent.com/clementgre/PDF4Teachers/master/images/preview.png)

# Features

## Conversion and PDF Tools

- Convert a selection of images to a single PDF.
- Convert a directory containing subdirectories with images to a set of PDFs, with one PDF per subdirectory and one page
  per image.
- Rotate, move, and delete PDF pages. Multiple pages can be selected at once in PDF edit mode.
- Insert pages from another PDF or from images into an existing PDF.
- Export a PDF page to an image with a custom resolution.
- Split a PDF into multiple PDFs based on automatically detected pages by color or based on user selection. Users can
  define names for the resulting PDFs and import names from a file.
- Create a booklet from a PDF or disassemble a booklet into a PDF with various options and support for two-page copies.
- Add margins to pages or crop pages, with options for different margins on each side of the page.
- Crop pages based on a mouse selection.

## Text elements

- Memorizes all added text elements in a previous and favorite text list, allowing quick insertion.
- Writing an element highlights similar elements in the list, which can be selected with the keyboard.
- Supports LaTeX and LibreOffice Math equations using the $$ or && delimiter or defining them as default writing mode.
- Favorite and previous texts can be saved and loaded in an element list.
- Textual elements can be written on multiple lines and wrapped to a maximum width.
- Supports custom fonts, including system-installed fonts.

## Numerical grading

- Ability to define a custom grade scale and copy it to other documents of the same assessment, with the ability to
  memorize grades position on document.
- Ability to define custom grade size, color, font, and showing option for each level of grade.
- Automatically numerates questions and calculates parent grades.
- Export of grades to a .csv spreadsheet, including comments, a set of documents in the same spreadsheet, or a set of
  documents in different csv files.
- Ability to quickly insert grades, with automatic prompts for the value of the next grade, shortcuts, and mouse grade definition.

## Competency-based grading

- Support for competency-based grading with custom levels of competencies (images, colors, text, etc.).
- Ability to define different competencies for each assessment, which are evaluated using the different levels of
  achievement.
- Competency table generated on the output PDF.
- Support for importing/exporting competency spreadsheets from the software SACoche.
- Export of results to a .csv spreadsheet.

## Vector elements, freeform drawing and images

- Support for vector elements, including SVG path elements, with custom colors and thicknesses.
- Lists for previously added and favorite vector elements.
- Options for vector elements that make it easier to use highlighters, arrows, underlines, and more.
- Support for freeform drawing with custom colors and thicknesses.
- Automatic page switching and element splitting for freeform drawing based on time, length, or mouse movements to prevent elements from being too long.
- Support for images with custom sizes and positions. Images are not stored in the edit, and a reference to the original
image is kept.
- Image gallery and favorite images list.
- Predefined vector elements including icons, shapes, arrows and more.

## Interface

- Pretty and intuitive interface, with a dark theme and support for OS theme sync.
- Full support for undo/redo operations.
- Support for copy/paste elements between PDFs and between applications.
- Available languages : English, French, Italian, [add your language !](https://weblate.pdf4teachers.org/projects/)

# Contribute

## Translate the app, website or documentation

All translations can be done on [Weblate](https://weblate.pdf4teachers.org/projects/).

Otherwise, you can also translate the app by yourself, by editing the files in the `src/main/resources/translations`
folder.

See [pdf4teachers.org/Contribute](https://pdf4teachers.org/Contribute/) for more informations.

## Contribute to the code

PDF4Teachers has now achieved all the main goals of the project, but is still maintained and improved. If you want to
contribute, you will need a lot of time to deep dive into the code (see <a href="#code-organization">Code
organization</a>), you can then open a pull request.

As detailed in the <a href="#dependencies">Dependencies</a> section, PDF4Teachers uses gradlew and the source code can
be run with `gradlew run` with the JDK 21 in your ``JAVA_HOME`` environment variable.

See [ClementGre/PDF4Teachers-Website](https://github.com/ClementGre/PDF4Teachers-Website) for the PDF4Teachers' website
repository.

# Dependencies

The application was developed with JavaFX framework, in Java SE 21.

### Java dependencies

- **[JavaFX 21](https://openjfx.io/)** : The java API to create windows (GUI), and all the interfaces with the user.
- **[Apache PDFBOX 3.0.2](https://pdfbox.apache.org/)** : used for all the interactions with PDFs: it generates images
  from PDF file, add it add the texts/images of the editing on the PDF document while exporting. **Commons Logging**, *
  *Font BOX**, **JAI ImageIO** and **jbig2-imageio**, are dependencies of PDF Box.
- **[JMetro 11.6.15](https://pixelduke.com/java-javafx-theme-jmetro/)** : JavaFX theme. It offers you a nicer interface,
  including the dark mode.
- **[ControlsFX 11.1.0](https://controlsfx.github.io/)** : JavaFX new inputs and custom panes
- **[Writer2Latex 1.6.1](http://writer2latex.sourceforge.net/)** : Used to convert StarMath (or LibreOffice Math)
  language in LaTeX for rendering.
- **[JLatexMath 1.0.7](https://github.com/opencollab/jlatexmath)** : Used to generate images from LaTeX expressions for
  exportation.
- **[SnakeYAML 1.30](https://bitbucket.org/asomov/snakeyaml/src/master/)** : Lets read YAML files easier (edits + user
  datas) : it converts the YAML into ``HashMap<String, Object>`` and vice versa.
- **[Opencsv 5.6](http://opencsv.sourceforge.net/)** : used to read/write CSV files (when importing SACoche assessment)
- **[Jackson Streaming API 2.13.3](https://github.com/FasterXML/jackson-core)** : lets read the JSON format. Used to
  parse GitHub responses when checking if updates are available, and used for automatic update of translations.
- **[Metadata-Extractor 2.18.0](https://drewnoakes.com/code/exif/)** : Used to read the images EXIF data, so
  PDF4Teachers can take in account the rotation of images
- **[Batik Parser 1.14](https://xmlgraphics.apache.org/batik/using/parsers.html)** : Allow me to parse SVG easily, with
  custom handlers for SVG importation.
- **[PdfBox Graphics2D 0.40](https://github.com/rototor/pdfbox-graphics2d)** : Used to write SVG to a PDF, using PDFBox.
- **[Google Diff-Match-Patch 0.1](https://github.com/google/diff-match-patch)** : Allow me to perform some actions on
  strings for undo/redo on text inputs.
- **[Unique4J 1.4](https://github.com/prat-man/unique4j)** : Used to set up a single instance of the app when opening
  files from system.
- **[jSystemThemeDetector 3.8](https://github.com/Dansoftowner/jSystemThemeDetector)** : Used to synchronize the app
  theme with the system theme (light or dark).

### Gradle plugins

The dependencies of the application are managed by [Gradle](https://gradle.org/) 7 (rc-2), therefore, you can
execute ``./gradlew run`` (bash) or ``gradlew.bat run`` (batch) in a command prompt to run the application using only
the downloaded code, and the Java JDK 21 (should be in the environment variable ``JAVA_HOME``). Gradle is using some
plugins to manage dependencies:

- **[Badass JLink Plugin](https://github.com/beryx/badass-jlink-plugin/)** allows to use JLink and JPackager (
  distribution tools) directly with gradle.
- **[JavaFX Gradle 7 Plugin](https://github.com/xzel23/javafx-gradle-plugin)** fork of the original JavaFX Gradle
  Plugin, allows to setup the JavaFX libs with Gradle 7 and JDK 16+
- **[Extra Java Module Info](https://github.com/jjohannes/extra-java-module-info)** lets us customize module-info of any
  jar file, and then, it allows me to use non modular dependencies (Allows me to give a name to any "unnamed module").

### Packaging tool

- **[JLink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)** is a
  distribution tool used to generate an image of the code including the dependencies and the used modules of the JRE,
  for each platform (exports all the code and the JRE so there is no need to have Java installed to run PDF4Teachers). (
  JDK tool)
- **[JPackage](https://docs.oracle.com/en/java/javase/14/docs/specs/man/jpackage.html)** is used to generate Windows
  .msi and
  Linux .deb installers for each platform from the image that was generated by JLink. (JDK tool)
- **[DMGCanvas](https://www.araelium.com/dmgcanvas)** allows to create a custom OSX DMG file, from the .app created
  by hand from JLink image. Needs to be installed to use the ``gradlew autoPackage`` task on Mac OSX.
- **[WiX Toolset](https://wixtoolset.org/)** is a JPackage dependency, it allows to build the .msi file. Needs to be
  installed to use the ``gradlew autoPackage`` task on Windows.

The installer can be build automatically using the custom ``autoPackage`` task on any platform.

### Fonts for the documentations files

If you have to work on a documentation translation, it is always better to have the right fonts installed to have the
correct layout (and if you eventually have to generate the PDF from it).
Then you will need these fonts :

- Arial Rounded MT Bold
- Calibri
- Liberation Sans

# [Code Organization](https://github.com/ClementGre/PDF4Teachers/tree/master/src/main/java/fr/clementgre/pdf4teachers)


