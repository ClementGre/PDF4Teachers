## fr.clementgre.pdf4teachers.utils

All the utilitary classes.

- ``TextWrapper`` can wrap text with a max width and a precise font.

- ``Stringutils`` do actions about Strings :
    - Special replacements like remove all text after/before last key char(s)
    - Increment a name ex: "1" -> "2" ; "test A" -> "text B"
    - Parsing Integers, Doubles, Boolean with possibility to got null or not.
    - Clamping a number ( Math.max() + Math.min() )

- ``PlatformUtils`` helps to do things about JavaFX and the lowers levels, like managing threads and opening files on
  system file explorer.

- ``PaneUtils`` is used to define Panes dimensions/padding faster and some others things.

- ``FontUtils`` can load fonts, check if a font is italic/bold etc. it contains a list of all the fonts used. (.ttf in
  the ressource directory).

- ``FilesUtils`` can count the size of a directory, get a file extension, replace user.home by ~ of a file path etc.

- ``.style`` manage the JMetro Style API and load the .css files. It can also switch colors to adapt them to the theme

- ``.sort`` provide a sort panel API. .sort.Sorter provide functions to sort Files and TextElements with criteria.

- ``.objects`` is just objects that store vars, like PositionDimentions that store a width, a height, a x and a y.

- ``.interfaces`` contains various types of callBack interfaces, but also a custom JavaFX StringConverter<Double>
  . ``.interfaces.TwoStepListAction`` is used to make an action with a List in two steps. It is used to export PDF : the
  prepare() method is registering all the files that will be exported, the sortData() is used to remove files that will
  not be exported, and to check if files already exists etc. And completeData() is generating the PDF. completeData()
  could be async with an alert indicating the status of the operation.

- ``.image`` is generating images with special dimensions, it is managing colors adjust with the choosed theme and it is
  storing and generating SVG images with special dimensions/ratio.

- ``.dialog`` can generate Dialogs and Alerts fastly. AlreadyExistDialog is complete dialog for the file saving, to
  chose to stop all, skip, erase, rename...