## fr.clementgre.pdf4teachers.datasaving

Theses classes are used to store all data excluding Languages and Editions. It uses the files userdata.yml, settings.yml
and textelements.yml.

The data is loaded/saved with SnakeYAML who transform YAML to HashMap<String, Object> and vice versa. Config is an "
utils" used to edit theses HashMap easierly.

#### Reflection

UserData.java and Settings.java uses reflection to store and load vars automaticaly. They use Annotations UserDataObject
and SettingObject.

- UserDataObject is storing the path of the var in the yaml and the type of the var is detected with reflection.
- For the Settings, there is BooleanSetting, StringSetting etc. who extens Setting. Setting is storing the path in the
  YAML hierarchy, but also a title, description, an image etc. because almost all Setting is showed in the MenuBar (
  .panel.MenuBar).

TextElementsData does not use reflexion since it is storing only the previous, favorite and lists elements.

#### Async ?

Settings load on the JavaFX Thread by Main.java, and they are saved everytime one is changed. Therefore, the Settings
are always accessible.

UserData and TextElements load and save asyncly. It save only when closing MainWindow or every minute. Because they are
loading data asyncly, we never get vars, it is UserData or TextElementsData who copy the loaded data into vars of
anothers Class when load. When saving, he is copying back the vars.


