## Code documentation / details

Main.java (extends Application) is the Main file with the Java start function and the JavaFX start method. Main is
initialising MainWindow after checking/defining the language of the app and after initialising Settings (settings.yml).

#### Packages

- ``fr.clementgre.pdf4teachers.components`` : Custom JavaFX components (extends an instanceof Control).
- ``fr.clementgre.pdf4teachers.datasaving`` : Classes that saves app data in YAML files.
- ``fr.clementgre.pdf4teachers.document`` : Everything that manage the current document (Editions (load, save, export,
  import)
  & Elements, Page render, Exporting, Converting).
- ``fr.clementgre.pdf4teachers.interfaces`` : Mainly external Windows (extends Stage), Tips
  system, Keyboard shortcuts.
- ``fr.clementgre.pdf4teachers.panel`` : All panes of the app (MainScreen, BottomBar, MenuBar, LeftBar + Tabs + Tabs
  elements/actions).
- ``fr.clementgre.pdf4teachers.utils`` : Utility classes  (Threading, Dialogs, Interfaces, Style, Fonts, Strings &
  Integers...)
