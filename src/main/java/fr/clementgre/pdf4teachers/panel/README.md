## fr.clementgre.pdf4teachers.panel

- ``.MainScreen.`` : MainScreen.java represents the Panel that shows the opened document. It manages the document
  opening, saving, showing status... ZoomOperator is the class that manage all the scroll/zoom system (this system is
  coded handly).

- ``MenuBar`` : The app top MenuBar. Define some events actions and all the customs MenuItem generation. It manage also
  the OSX menu bar.

- ``FooterBar`` : The app that shows information at the botom of the app.

### ``.sidebar.``

- ``.leftBar.files.`` : The files tab, the FileListView uses (FileListItem extens ListCell).
- ``.sidebar.texts.`` : The texts elements tab, in ``.TreeViewSections.`` are the classes that extends TreeItem. They
  are the 3 main trees of the elements tab. There is also ListsManager for the elements saving system. The TreeView
  works like the ListView of the File tab (But it is kind of more complex).
- ``.sidebar.grades.`` : The grades tab, manage CSV exporting and grade scale copying. It also manages the grades
  settings and obviusly, all the components of the Tab, with one instance of GradeTreeItem per GradeElement. The
  var ``core`` contains the coresponding GradeElement.
- ``.sidebar.paint.`` : The future paint tab.
