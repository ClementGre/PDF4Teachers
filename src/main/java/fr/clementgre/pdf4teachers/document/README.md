## fr.clementgre.pdf4teachers.document

- ``Document`` : Represents an opened document. Manage the Edition loading with an instance of ``.editions.Edition`` and contains all instances of ``.render.display.PageRenderer extends Pane`` who represents the page pane. it contains also an instance of ``.render.display.PDFPageRenderer`` who renders pages to images with the PDFBox API. Document is making a link between MainScreen and Edition, PageRenderer. The var document into ``MainScreen`` is ``null`` when no document is open.

- ``.editions.`` : classes to load/save/export/import editions and Elements classes.


- ``.render.`` : classes to render and display the PDF pages (some of them extends control). + classes and windows to convert and export.