## fr.clementgre.pdf4teachers.components

Each class extends a JavaFX component. It's a "utils package" with cutom JavaFX components.

- The `ModeMenuItem` and `NodeRadioMenuItems` are used to use Tooltips on MenuItem.
- `ScratchText` is used to bypass the JMetro theme by using a class not named Text, then not having the css styles
  applied.
- `SyncColorPicked` synchronise it's custom colors with a static var. Therefore, `UserData` can save and load them.
