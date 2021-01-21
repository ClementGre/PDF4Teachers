## fr.clementgre.pdf4teachers.document.render

### ``.display.`` :

- ``PageEditPane extends VBox`` : the pane which contains all the page actions buttons (on the top left of a page).

- ``PageRenderer extends Pane`` : the page pane. One instance per page is containing into ``Document``. ``PageRenderer`` contains all it's elements into an ArrayList<>. Contains also ``PageEditPane`` and ``PageZoneSelector``.

- ``enum PageStatus`` : the differents status that a ``PageRenderer`` can have (rendered, rendering, hided, failed).

- ``PageZoneSelector`` : a children of ``PageRenderer`` that can be used to ask the user to select a zone on the page (used for screenshots).

- ``PDFPagesEditor`` : instancied by ``PDFPagesRenderer``, offers methods to rotate, add, remove, convert, move and capture pages.

- ``PDFPagesRenderer`` : instancied by ``Document``, can render a PDF page in an image into a specified size. ``PageRenderer`` uses it's methods to get their background image.

### ``.convert.`` :

- ``ConvertedFile`` : just contains a ``PDDocument`` and a ``File``. Offers methods to add pages.
- ``ConvertDocument`` : call ``ConvertWindow`` and save the returned ``ConvertedFile.document`` into ``ConvertedFile.file``.
- ``ConvertWindow`` : Open the convert widow and return a ``CallBackArg<ArrayList<ConvertedFile>>``. Call ``ConvertRenderer`` when needed. Can be called by ``ConvertDocument`` to convert images to PDF or by ``PDFPagesEditor``  to convert images to ``PDPages`` to add in an existing document.
	- ``ConvertRenderer`` : Render asyncly images to ``PDDocument`` stored into a ``ConvertedFile``. Need an instance of ``ConvertWindow``.

### ``.export.`` :

- ``ExportWindow`` : window to export a/some documents. Call ``ExportRenderer`` when needed.

- ``ExportRenderer`` : render a PDF into a new PDF with the edition integrated. Load the edition(s) with a function of ``Edition`` which gives an array of all ``Element`` classes (not attached to a page). Call ``xxxElementRenderer`` to render each type of elements.