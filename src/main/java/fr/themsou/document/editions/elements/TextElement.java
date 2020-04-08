package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

public class TextElement extends Text implements Element {

	private IntegerProperty realX = new SimpleIntegerProperty();
	private IntegerProperty realY = new SimpleIntegerProperty();
	private PageRenderer page;
	private ObjectProperty<Font> realFont = new SimpleObjectProperty<>();

	private Rectangle border = new Rectangle(0, 0, Color.TRANSPARENT);

	ContextMenu menu = new ContextMenu();

	private int pageNumber;
	private int shiftX = 0;
	private int shiftY = 0;

	public TextElement(int x, int y, Font font, String text, Color color, int pageNumber, PageRenderer page) {

		this.pageNumber = pageNumber;
		this.realX.set(x);
		this.realY.set(y);

		setRealFont(font);
		setText(text);
		setTextOrigin(VPos.BOTTOM);
		setFill(color);

		setBoundsType(TextBoundsType.VISUAL);

		if(page == null) return;

		fontProperty().bind(Bindings.createObjectBinding(() -> {
			return translateFont(getRealFont());
		}, realFontProperty(), Main.mainScreen.zoomProperty()));

		this.page = page;

		layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(500.0)));
		layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(800.0)));

		setCursor(Cursor.MOVE);

		// enable shadow if this element is selected
		TextElement thisObject = this;
		Main.mainScreen.selectedProperty().addListener(new ChangeListener<Element>() {
			@Override public void changed(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {
				if(oldValue == thisObject && newValue != thisObject){
					setEffect(null);
					menu.hide();
				}else if(oldValue != thisObject && newValue == thisObject){
					DropShadow ds = new DropShadow();
					ds.setOffsetY(3.0f);
					ds.setColor(Color.color(0f, 0f, 0f));
					setEffect(ds);
					setCache(true);
					requestFocus();
				}
			}
		});
		NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Supprimer"), -1, false);
		item1.setAccelerator("Suppr");
		item1.setToolTip(TR.tr("Supprime cet élément. Il sera donc retiré de l'édition."));
		NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Dupliquer"), -1, false);
		item2.setToolTip(TR.tr("Crée un second élément identique à celui-ci."));
		NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments précédents"), -1, false);
		item3.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments précédents."));
		NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments Favoris"), -1, false);
		item4.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments favoris."));
		menu.getItems().addAll(item1, item2, item4, item3);
		Builders.setMenuSize(menu);

		item1.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				delete();
			}
		});
		item2.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				PageRenderer page = Main.mainScreen.document.pages.get(0);
				if (Main.mainScreen.document.getCurrentPage() != -1)
					page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

				TextElement realElement = (TextElement) thisObject.clone();
				realElement.setRealX(realElement.getRealX() + 10);
				realElement.setRealY(realElement.getRealY() + 10);
				page.addElement(realElement, true);
				Main.mainScreen.selectedProperty().setValue(realElement);
			}
		});
		item3.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				Main.lbTextTab.addSavedElement(thisObject.toNoDisplayTextElement(NoDisplayTextElement.LAST_TYPE, true));
			}
		});
		item4.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				Main.lbTextTab.addSavedElement(thisObject.toNoDisplayTextElement(NoDisplayTextElement.FAVORITE_TYPE, true));
			}
		});

		setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override public void handle(MouseEvent e){
				e.consume();

				shiftX = (int) e.getX();
				shiftY = (int) e.getY();
				menu.hide();
				select();

				if(e.getButton() == MouseButton.SECONDARY){
					menu.show(page, e.getScreenX(), e.getScreenY());
				}
			}
		});
		setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent e) {
				if(e.getCode() == KeyCode.DELETE){
					Main.mainScreen.setSelected(null);
					delete();
				}
			}
		});

		setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {

				double itemX = getLayoutX() + e.getX() - shiftX;
				double itemY = getLayoutY() + e.getY() - shiftY;

				boolean changePage = false;
				if(thisObject.page.mouseY < -30){
					if(thisObject.page.getPage() > 0){

						Main.mainScreen.setSelected(null);

						thisObject.page.removeElement(thisObject, false);
						thisObject.page = Main.mainScreen.document.pages.get(thisObject.page.getPage() -1);
						thisObject.page.addElement(thisObject, false);

						itemY = thisObject.page.getHeight() - getLayoutBounds().getHeight();
						changePage = true;
					}
				}else if(thisObject.page.mouseY > thisObject.page.getHeight() + 30){
					if(thisObject.page.getPage() < Main.mainScreen.document.pages.size()-1){

						Main.mainScreen.setSelected(null);

						thisObject.page.removeElement(thisObject, false);
						thisObject.page = Main.mainScreen.document.pages.get(thisObject.page.getPage() + 1);
						thisObject.page.addElement(thisObject, false);

						itemY = 0;
						changePage = true;
					}
				}

				checkLocation(itemX, itemY);

				if(changePage){
					Main.lbTextTab.onFileTextSortManager.simulateCall();
				}

			}
		});

		textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Edition.setUnsave();

				if(getLayoutY() < getLayoutBounds().getHeight()){
					checkLocation(getLayoutX(), getLayoutY());
				}
			}
		});
	}

	public void checkLocation(double itemX, double itemY){

		double linesHeight = getLayoutBounds().getHeight();
		if(itemY < linesHeight) itemY = linesHeight;
		if(itemY > page.getHeight()) itemY = page.getHeight();
		if(itemX < 0) itemX = 0;
		if(itemX > page.getWidth() - getLayoutBounds().getWidth()) itemX = page.getWidth() - getLayoutBounds().getWidth();

		realX.set((int) (itemX / page.getWidth() * 500.0));
		realY.set((int) (itemY / page.getHeight() * 800.0));

	}

	@Override
	public void select() {

		Main.mainScreen.setSelected(this);
		Main.lbTextTab.selectItem();
		toFront();
		requestFocus();
		Edition.setUnsave();
	}

	public NoDisplayTextElement toNoDisplayTextElement(int type, boolean hasCore){
		if(hasCore) return new NoDisplayTextElement(getRealFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new NoDisplayTextElement(getRealFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000);
	}
	@Override
	public void delete() {
		page.removeElement(this, true);
	}

	@Override
	public void writeSimpleData(DataOutputStream writer) throws IOException {
		writer.writeByte(1);
		writeData(writer);
	}
	public void writeData(DataOutputStream writer) throws IOException {
		writer.writeByte(page.getPage());
		writer.writeShort(getRealX());
		writer.writeShort(getRealY());
		writer.writeFloat((float) getRealFont().getSize());
		writer.writeBoolean(getFontWeight(getRealFont()) == FontWeight.BOLD);
		writer.writeBoolean(getFontPosture(getRealFont()) == FontPosture.ITALIC);
		writer.writeUTF(getRealFont().getFamily());
		writer.writeByte((int) (((Color) getFill()).getRed() * 255.0 - 128));
		writer.writeByte((int) (((Color) getFill()).getGreen() * 255.0 - 128));
		writer.writeByte((int) (((Color) getFill()).getBlue() * 255.0 - 128));
		writer.writeUTF(getText());
	}

	public static TextElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException {

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		double fontSize = reader.readFloat();
		boolean isBold = reader.readBoolean();
		boolean isItalic = reader.readBoolean();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String text = reader.readUTF();

		Font font = getFont(fontName, isItalic, isBold, (int) fontSize);

		return new TextElement(x, y, font, text, Color.rgb(colorRed, colorGreen, colorBlue), page, hasPage ? Main.mainScreen.document.pages.get(page) : null);

	}
	public static void consumeData(DataInputStream reader) throws IOException {
		reader.readByte();reader.readShort();reader.readShort();
		reader.readFloat();reader.readBoolean();reader.readBoolean();reader.readUTF();
		reader.readByte();reader.readByte();reader.readByte();
		reader.readUTF();
	}
	public static void readDataAndCreate(DataInputStream reader) throws IOException {

		TextElement element = readDataAndGive(reader, true);

		if(Main.mainScreen.document.pages.size() > element.page.getPage())
			Main.mainScreen.document.pages.get(element.page.getPage()).addElementSimple(element);

	}



	public int getRealX() {
		return realX.get();
	}
	public IntegerProperty RealXProperty() {
		return realX;
	}
	public void setRealX(int x) {
		this.realX.set(x);
	}

	public int getRealY() {
		return realY.get();
	}
	public IntegerProperty RealYProperty() {
		return realY;
	}
	public void setRealY(int y) {
		this.realY.set(y);
	}

	public Font getRealFont() {
		return realFont.get();
	}

	public ObjectProperty<Font> realFontProperty() {
		return realFont;
	}

	public void setRealFont(Font realFont) {
		this.realFont.set(realFont);
	}

	private Font translateFont(Font font) {

		boolean bold = false;
		if(TextElement.getFontWeight(font) == FontWeight.BOLD) bold = true;
		boolean italic = false;
		if(TextElement.getFontPosture(font) == FontPosture.ITALIC) italic = true;

		return getFont(font.getFamily(), italic, bold, (int) (font.getSize() / 75.0 * Main.mainScreen.getZoom()));
	}

	public static Font getFont(String family, boolean italic, boolean bold, double size){

		InputStream fontFile = TextElement.class.getResourceAsStream("/fonts/" + getFontPath(family, italic, bold));

		if(fontFile == null) fontFile = TextElement.class.getResourceAsStream("/fonts/" + getFontPath(family, italic, false));

		return Font.loadFont(fontFile, size);
	}
	public static String getFontPath(String family, boolean italic, boolean bold){

		String fileName = "";
		if(bold) fileName += "bold";
		if(italic) fileName += "italic";
		if(fileName.isEmpty()) fileName = "regular";

		return family + "/" + fileName + ".ttf";
	}

	public static FontWeight getFontWeight(Font font) {

		String[] style = font.getStyle().split(" ");
		if(style.length >= 1){
			if(style[0].equals("Bold")){
				return FontWeight.BOLD;
			}
		}

		return FontWeight.NORMAL;
	}
	public static FontPosture getFontPosture(Font font) {

		String[] style = font.getStyle().split(" ");
		if(style.length == 1){
			if(style[0].equals("Italic")){
				return FontPosture.ITALIC;
			}
		}else if(style.length == 2){
			if(style[1].equals("Italic")){
				return FontPosture.ITALIC;
			}
		}

		return FontPosture.REGULAR;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}
	@Override
	public int getCurrentPageNumber() {
		return page.getPage();
	}

	@Override
	public Element clone() {
		return new TextElement(getRealX(), getRealY(), getRealFont(), getText(), (Color) getFill(), pageNumber, page);
	}

}