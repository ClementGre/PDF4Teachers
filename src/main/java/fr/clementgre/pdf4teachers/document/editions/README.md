## fr.clementgre.pdf4teachers.document.editions

- ``Edition`` : Functions to find an edit file from a pdf file... + Functions to count elements in an edition (used by ``FilesTab``) + Methods to load edition. ``Edition`` is stored into ``Document`` who call the load and save methods.

- ``EditionExporter`` : Manage the exportation and importation of editions, including dialogs.

### ``.elements.`` :

- ``Element extends Region`` : represents any type of element that can be added into a page. Cntains the common methods.

- ``GradeElement extends Element`` : represents a grade element.

- ``TextElement extends Element`` : represents a text element.

- ``GraphicElement extends Element`` : represents a resizeable element (vectors and Images : ``VectorElement extends GraphicElement`` and ``ImageElement extends GraphicElement``).