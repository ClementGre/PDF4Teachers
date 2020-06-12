package fr.themsou.panel.leftBar.texts;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.themsou.utils.Builders;
import fr.themsou.utils.FontUtils;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import fr.themsou.utils.components.ScratchText;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class TextTreeItem extends TreeItem{

	private ObjectProperty<Font> font = new SimpleObjectProperty<>();
	private String text;
	private ObjectProperty<Color> color = new SimpleObjectProperty<>();

	private int type;
	private long uses;
	private long creationDate;

	// Graphics items
	public HBox pane = new HBox();
	public ImageView linkImage = Builders.buildImage(getClass().getResource("/img/TextTab/link.png")+"", 0, 0);
	public ScratchText name = new ScratchText();
	public ContextMenu menu;
	public EventHandler<MouseEvent> onMouseCLick;

	// Link
	private TextElement core = null;
	private ChangeListener<String> textChangeListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
		setText(newValue); updateGraphic();
	};
	private ChangeListener<Paint> colorChangeListener = (ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) -> {
		setColor((Color) newValue);
	};


	public TextTreeItem(Font font, String text, Color color, int type, long uses, long creationDate) {
		this.font.set(font);
		this.text = text;
		this.color.set(color);
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;

		setup();
		updateGraphic();
	}
	public TextTreeItem(Font font, String text, Color color, int type, long uses, long creationDate, TextElement core) {
		this.font.set(font);
		this.text = text;
		this.color.set(color);
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;
		this.core = core;

		setup();
	}

	public void setup(){

		if(core != null){
			// bindings with the core
			fontProperty().bind(core.fontProperty());
			core.textProperty().addListener(textChangeListener);
			core.fillProperty().addListener(colorChangeListener);
		}

		// Setup les éléments graphiques
		menu = TextTreeView.getNewMenu(this);

		onMouseCLick = (MouseEvent mouseEvent) -> {
			if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
				addToDocument(false);
				// Update the sorting if is sort by utils
				if(getType() == TextTreeSection.FAVORITE_TYPE){
					if(MainWindow.lbTextTab.treeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.tr("Utilisation"))){
						MainWindow.lbTextTab.treeView.favoritesSection.sortManager.simulateCall();
					}
				}else if(getType() == TextTreeSection.LAST_TYPE){
					if(MainWindow.lbTextTab.treeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.tr("Utilisation"))){
						MainWindow.lbTextTab.treeView.lastsSection.sortManager.simulateCall();
					}
				}
			}
		};
		name.setFill(StyleManager.convertColor(color.get()));
		colorProperty().addListener((observable, oldValue, newValue) -> {
			name.setFill(StyleManager.convertColor(newValue));
		});

		name.fontProperty().bind(Bindings.createObjectBinding(this::getListFont, fontProperty(), Main.settings.smallFontInTextsListProperty()));

		updateIcon();
	}

	public void updateGraphic(){ // Re calcule le Text

		int cellHeight = (Main.settings.isSmallFontInTextsList() ? 14 : 18);

		int maxWidth = (int) (MainWindow.lbTextTab.treeView.getWidth() - 45);
		if(maxWidth < 0) return;

		Font font = getListFont();
		String wrappedText = "";
		final String[] splittedText = getText().split("\\n");

		if(splittedText.length != 0){
			if(Main.settings.isShowOnlyStartInTextsList()){

				String text = splittedText[0];
				wrappedText += new TextWrapper(text, font, maxWidth).wrapFirstLine();
				text = text.replaceFirst(Pattern.quote(wrappedText), "");

				// SECOND LINE
				if(!text.isEmpty()){
					String wrapped = new TextWrapper(text, font, maxWidth - 13).wrapFirstLine();
					wrappedText += "\n" + wrapped;
					if(!text.replaceFirst(Pattern.quote(wrapped), "").isBlank()) wrappedText += "...";
				}else if(splittedText.length > 1){
					String wrapped = new TextWrapper(splittedText[1], font, maxWidth - 13).wrapFirstLine();
					wrappedText += "\n" + wrapped;
					if(!splittedText[1].replaceFirst(Pattern.quote(wrapped), "").isBlank()) wrappedText += "...";
				}
			}else{
				for(String text : splittedText){
					wrappedText += wrappedText.isEmpty() ? new TextWrapper(text, font, maxWidth).wrap() : "\n" + new TextWrapper(text, font, maxWidth).wrap();
				}
			}
		}

		name.setText(wrappedText);
		name.setFill(StyleManager.convertColor(color.get()));

		pane.setAlignment(Pos.CENTER_LEFT);

	}
	Rectangle rect = new Rectangle();
	public void updateIcon(){ // Re définis les children de la pane
		int cellHeight = (Main.settings.isSmallFontInTextsList() ? 14 : 18);

		pane.getChildren().clear();

		HBox spacer = new HBox();
		spacer.setPrefWidth(15 + (3 + 4 + 3));
		spacer.setAlignment(Pos.TOP_RIGHT);
		if(core != null) spacer.getChildren().add(linkImage);


		rect.setWidth(4);
		rect.setHeight(4);
		rect.setFill(StyleManager.convertColor(Color.WHITE));
		HBox.setMargin(rect, new Insets(((cellHeight - 4) / 2.0), 3, 0, 3));
		spacer.getChildren().add(rect);

		pane.getChildren().addAll(spacer, name);

	}
	public void updateCell(TreeCell<String> cell){ // Réattribue une cell à la pane

		if(cell == null) return;
		if(name.getText().isEmpty()) updateGraphic();

		name.setFill(StyleManager.convertColor(color.get()));
		rect.setFill(StyleManager.convertColor(Color.WHITE));

		cell.setGraphic(pane);
		cell.setStyle(null);
		cell.setStyle("-fx-padding: 0 -35;");
		cell.setContextMenu(menu);
		cell.setOnMouseClicked(onMouseCLick);

	}

	private Font getListFont(){
		return FontUtils.getFont(getFont().getFamily(), false, false, Main.settings.isSmallFontInTextsList() ? 12 : 14);
	}

	@Override
	public boolean equals(Object v){

		if(v instanceof TextTreeItem){
			TextTreeItem element = (TextTreeItem) v;
			if(element.type == type && element.text.equals(text) && element.color.hashCode() == color.hashCode()){
				if(element.font.get().getStyle().equals(font.get().getStyle()) && element.font.get().getSize() == font.get().getSize() && element.getFont().getFamily().equals(font.get().getFamily())){
					return true;
				}
			}
		}
		return false;
	}

	public TextTreeItem clone(){
		return new TextTreeItem(font.get(), text, color.get(), type, uses, creationDate);
	}

	public LinkedHashMap<Object, Object> getYAMLData(){
		LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
		data.put("color", color.get().toString());
		data.put("font", font.get().getFamily());
		data.put("size", font.get().getSize());
		data.put("bold", FontUtils.getFontWeight(font.get()) == FontWeight.BOLD);
		data.put("italic", FontUtils.getFontPosture(font.get()) == FontPosture.ITALIC);
		data.put("uses", uses);
		data.put("date", creationDate);
		data.put("text", text);

		return data;
	}
	public TextElement toRealTextElement(int x, int y, int page){
		return new TextElement(x, y, page, font.get(), text, color.get(), true);
	}
	public static TextTreeItem readDataAndGive(DataInputStream reader, int type) throws IOException {

		double fontSize = reader.readFloat();
		boolean isBold = reader.readBoolean();
		boolean isItalic = reader.readBoolean();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		long uses = reader.readLong();
		long creationDate = reader.readLong();
		String text = reader.readUTF();

		Font font = FontUtils.getFont(fontName, isBold, isItalic, (int) fontSize);

		return new TextTreeItem(font, text, Color.rgb(colorRed, colorGreen, colorBlue), type, uses, creationDate);
	}
	public static TreeItem<String> readYAMLDataAndGive(HashMap<String, Object> data, int type){

		double fontSize = Config.getDouble(data, "size");
		boolean isBold = Config.getBoolean(data, "bold");
		boolean isItalic = Config.getBoolean(data, "italic");
		String fontName = Config.getString(data, "font");
		Color color = Color.valueOf(Config.getString(data, "color"));
		long uses = Config.getLong(data, "uses");
		long creationDate = Config.getLong(data, "date");
		String text = Config.getString(data, "text");

		Font font = FontUtils.getFont(fontName, isBold, isItalic, (int) fontSize);

		return new TextTreeItem(font, text, color, type, uses, creationDate);
	}

	public TextListItem toTextItem(){
		return new TextListItem(font.get(), text, color.get(), uses, creationDate);
	}

	public void addToDocument(boolean link){

		if(MainWindow.mainScreen.hasDocument(false)){
			uses++;
			PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
			if(MainWindow.mainScreen.document.getCurrentPage() != -1)
				page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

			int y = (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight());
			int x = (int) ((page.getMouseX() <= 0 ? 60 : page.getMouseX()) * Element.GRID_WIDTH / page.getWidth());
			TextElement realElement = toRealTextElement(x, y, page.getPage());

			if(link){
				// UnBind with the core
				if(core != null){
					fontProperty().unbind();
					core.textProperty().removeListener(textChangeListener);
					core.fillProperty().removeListener(colorChangeListener);
				}

				core = realElement;
				setup();
			}

			page.addElement(realElement, true);
			MainWindow.mainScreen.selectedProperty().setValue(realElement);
		}

	}
	public void unLink() {

		fontProperty().unbind();
		core.textProperty().removeListener(textChangeListener);
		core.fillProperty().removeListener(colorChangeListener);

		this.core = null;
		setup();
	}

	public Font getFont() {
		return font.get();
	}
	public ObjectProperty<Font> fontProperty() {
		return font;
	}
	public void setFont(Font font) {
		this.font.set(font);
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Color getColor() {
		return color.get();
	}
	public ObjectProperty<Color> colorProperty() {
		return color;
	}
	public void setColor(Color color) {
		this.color.set(color);
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getUses() {
		return uses;
	}
	public void setUses(long uses) {
		this.uses = uses;
	}
	public long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	public TextElement getCore() {
		return core;
	}
}