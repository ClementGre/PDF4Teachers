## fr.clementgre.pdf4teachers.panel

- ``MainScreen`` : MainScreen.java represents the Panel that shows the opened document. It manages the document
  opening, saving, showing status... `ZoomOperator` is the class that manages the scroll/zoom system.

- ``MenuBar`` : The app top MenuBar. Also manages the OSX menu bar.

- ``FooterBar`` : The footer bar that shows information at the bottom of the app.

### ``.sidebar``

- ``.leftBar.files`` : The files tab, the `FileListView` uses `FileListItem extends ListCell`.
- ``.sidebar.texts`` : The texts elements tab, in ``.TreeViewSections`` are the classes that extends `TreeItem`. They
  are the 3 main trees of the elements tab. There is also ListsManager for the elements saving system. The TreeView
  works like the ListView of the File tab, but in a more elaborated way.
- ``.sidebar.grades`` : The grades tab, manages CSV exporting and grade scale copying. It also manages the grades
  settings and obviously, all the components of the Tab, with one instance of `GradeTreeItem` per `GradeElement`. The
  variable ``core`` contains the corresponding GradeElement.
- ``.sidebar.paint`` : The paint tab with titled foldable panes defined in `.lists` package. Classes of
  `.gridviewfactory` are used to render elements into the grid view.
- ``.sidebar.skills`` : Everything that manages skill grading, including data structures in `.data`,
  and parsers/writers in `.parsers`.
