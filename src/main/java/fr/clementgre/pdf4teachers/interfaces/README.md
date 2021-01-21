## fr.clementgre.pdf4teachers.interfaces

All the clases that interact with the user.

- ``.autotips`` manage the Auto Tips system with Tooltip. It can show Tooltip from precise actions or each x seconds at precise locations.

- ``Macro`` is a key listener for app keyboard shortcuts

- ``OSXTouchBarManager`` should manage the ToushBar for Mac Book Pro but this project is presently on hold.

- ``.windows`` contains some windows of the app :
	- ``.language`` all the classes that manage the language : the LanguageUpdater is checking for languages updates and it is sending statistics data. TR is loading translations files and translating sentences with TR.tr(). LanguageWindow is the window to choose a language. it also setup languages files and languages list in UserData.
	- ``.log`` is the log window. It redirect the logs to CustomPrintStream who copy them to a String and to the official console.
	- ``AboutWindow`` Show details and credits about the app, designed in FXML (c.g. ressources).
	- ``LicenseWindow`` Show license on first start (on hold)
	- ``MainWindow`` Main window of the app, load clases of fr.clementgre.pdf4teachers.panes and it load UserData asyncly...
	- ``UpdateWindow`` made the check update request and show data if there is a new release.