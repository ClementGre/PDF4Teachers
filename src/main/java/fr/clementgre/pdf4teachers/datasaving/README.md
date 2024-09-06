## fr.clementgre.pdf4teachers.datasaving

These classes are used to store all data excluding Languages and Editions. It uses the
files `userdata.yml`, `sync_userdata.yml`, `settings.yml`
and all `simpleconfigs` files.

The data is loaded/saved with SnakeYAML which transform YAML to HashMap<String, Object> and vice versa. Config is a
utility used to edit the HashMap structures more easily.

#### Reflection

`UserData.java`, `SyncUserData.java`, and `Settings.java` uses reflection to store and load variables automatically.
They use Annotations `UserDataObject`
or `SettingObject`.

- `UserDataObject` is storing the path of the variable in the yaml and the type of the variable is detected with
  reflection.
- For the `Settings`, there is `BooleanSetting`, `StringSetting` etc. who extends `Setting`. `Setting` is storing the
  path in the
  YAML hierarchy, but also a title, description, an image etc. because almost every `Setting` is shown in
  the `MenuBar` (`.panel.MenuBar`).

Classes that extends `SimpleConfig` in `.datasaving.simpleconfigs` do not use reflexion.
When a simple config is loaded, the data is directly loaded onto the app ui.
When the data is saved, the data is directly taken from the app ui.

#### Async ?

Settings load on the JavaFX Thread by `Main.java`, and they are saved everytime one is changed. Therefore, the Settings
are always accessible.
`SyncUserData` is loaded on the main thread like `Settings` in order to be available when the app starts. But it is
saved the same way as `UserData`.

`UserData` and `SimpleConfig` load and save async. They are saved along with `SyncUserData` only when
closing `MainWindow` or every minute. Because they are
loaded async.


