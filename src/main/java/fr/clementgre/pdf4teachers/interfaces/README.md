## fr.clementgre.pdf4teachers.interfaces

All the clases that interact with the user.

- ``.autotips`` manage the Auto Tips system with Tooltip. It can show Tooltip from precise actions or each x seconds at
  precise locations.

- ``KeyboardShortcuts`` is a key listener for app keyboard shortcuts

- ``CopyPasteManager`` receives copy/paste events and manage them. It try to apply the action on the currently focused
  control, and if it can't, it applies the action on the main window.

- `AutoHideNotificationPane` is the notification pane that can hide itself after a certain time, and that can be
  displayed in the bottom of the `MainScreen`.

- ``.windows`` contains of the app including:
    - ``.language`` all the classes that manage the language: `LanguageUpdater` is checking for languages updates and
      it sends analytics data. `TR` is loading translations files and translating sentences with `TR.tr()`.
      `LanguageWindow` is the window to choose a language. It also setups languages files and languages list
      in `UserData`.
    - ``.log`` is the log window. It redirects the logs to `CustomPrintStream` who copy them both to a `String` and the
      official console.
    - ``AboutWindow`` Show details and credits about the app, designed in FXML (see ressources directory).
    - ``LicenseWindow`` Show license on first start (on hold)
    - ``MainWindow`` Main window of the app. It initializes classes of `fr.clementgre.pdf4teachers.panes` and it loads
      UserData
      in an async way.
    - ``UpdateWindow`` made the check update request and show data if there is a new release.

Almost all the widows extends `.windows.AlternativeWindow`, who is a custom Stage with a custom buttons bar and title
bar.
