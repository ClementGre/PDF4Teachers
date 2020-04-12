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

	// Size for A4 - 200dpi
	public static final float GRID_WIDTH = 1654;
	public static final float GRID_HEIGHT = 2339;

	private IntegerProperty realX = new SimpleIntegerProperty();
	private IntegerProperty realY = new SimpleIntegerProperty();
	private PageRenderer page;

	ContextMenu menu = new ContextMenu();

	private int pageNumber;
	private int shiftX = 0;
	private int shiftY = 0;

	public TextElement(int x, int y, Font font, String text, Color color, int pageNumber, PageRenderer page) {

		this.pageNumber = pageNumber;
		this.realX.set(x);
		this.realY.set(y);

		setFont(font);
		setText(text);
		setTextOrigin(VPos.BOTTOM);
		setFill(color);

		setBoundsType(TextBoundsType.VISUAL);

		if(page == null) return;
		this.page = page;

		layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(TextElement.GRID_WIDTH)));
		layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(TextElement.GRID_HEIGHT)));

		setCursor(Cursor.MOVE);

		// enable shadow if this element is selected
		Main.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue == this && newValue != this){
				setEffect(null);
				menu.hide();
			}else if(oldValue != this && newValue == this){
				DropShadow ds = new DropShadow();
				ds.setOffsetY(3.0f);
				ds.setColor(Color.color(0f, 0f, 0f));
				setEffect(ds);
				setCache(true);
				requestFocus();
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

		item1.setOnAction(e -> delete());
		item2.setOnAction(e -> {
			PageRenderer page1 = Main.mainScreen.document.pages.get(0);
			if (Main.mainScreen.document.getCurrentPage() != -1)
				page1 = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

			TextElement realElement = (TextElement) this.clone();
			realElement.setRealX(realElement.getRealX() + 10);
			realElement.setRealY(realElement.getRealY() + 10);
			page1.addElement(realElement, true);
			Main.mainScreen.selectedProperty().setValue(realElement);
		});
		item3.setOnAction(e -> Main.lbTextTab.addSavedElement(this.toNoDisplayTextElement(NoDisplayTextElement.LAST_TYPE, true)));
		item4.setOnAction(e -> Main.lbTextTab.addSavedElement(this.toNoDisplayTextElement(NoDisplayTextElement.FAVORITE_TYPE, true)));

		setOnMousePressed(e -> {
			e.consume();

			shiftX = (int) e.getX();
			shiftY = (int) e.getY();
			menu.hide();
			select();

			if(e.getButton() == MouseButton.SECONDARY){
				menu.show(page, e.getScreenX(), e.getScreenY());
			}
		});
		setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.DELETE){
				Main.mainScreen.setSelected(null);
				delete();
			}
		});

		setOnMouseDragged(e -> {

			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			boolean changePage = false;
			if(this.page.mouseY < -30){
				if(this.page.getPage() > 0){

					Main.mainScreen.setSelected(null);

					this.page.removeElement(this, false);
					this.page = Main.mainScreen.document.pages.get(this.page.getPage() -1);
					this.page.addElement(this, false);

					itemY = this.page.getHeight();
					changePage = true;
				}
			}else if(this.page.mouseY > this.page.getHeight() + 30){
				if(this.page.getPage() < Main.mainScreen.document.pages.size()-1){

					Main.mainScreen.setSelected(null);

					this.page.removeElement(this, false);
					this.page = Main.mainScreen.document.pages.get(this.page.getPage() + 1);
					this.page.addElement(this, false);

					itemY = 0;
					changePage = true;
				}
			}

			checkLocation(itemX, itemY);

			if(changePage){
				layoutXProperty().bind(this.page.widthProperty().multiply(this.realX.divide(TextElement.GRID_WIDTH)));
				layoutYProperty().bind(this.page.heightProperty().multiply(this.realY.divide(TextElement.GRID_HEIGHT)));
				Main.lbTextTab.onFileTextSortManager.simulateCall();
			}

		});

		textProperty().addListener((observable, oldValue, newValue) -> {
			Edition.setUnsave();

			if(getLayoutY() < getLayoutBounds().getHeight()){
				checkLocation(getLayoutX(), getLayoutY());
			}
		});
	}

	public void checkLocation(double itemX, double itemY){

		double linesHeight = getLayoutBounds().getHeight();
		if(itemY < linesHeight) itemY = linesHeight;
		if(itemY > page.getHeight()) itemY = page.getHeight();
		if(itemX < 0) itemX = 0;
		if(itemX > page.getWidth() - getLayoutBounds().getWidth()) itemX = page.getWidth() - getLayoutBounds().getWidth();

		realX.set((int) (itemX / page.getWidth() * TextElement.GRID_WIDTH));
		realY.set((int) (itemY / page.getHeight() * TextElement.GRID_HEIGHT));

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
		if(hasCore) return new NoDisplayTextElement(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new NoDisplayTextElement(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000);
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
		writer.writeFloat((float) getFont().getSize());
		writer.writeBoolean(getFontWeight(getFont()) == FontWeight.BOLD);
		writer.writeBoolean(getFontPosture(getFont()) == FontPosture.ITALIC);
		writer.writeUTF(getFont().getFamily());
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

	/*private Font translateFont(Font font) {

		boolean bold = false;
		if(TextElement.getFontWeight(font) == FontWeight.BOLD) bold = true;
		boolean italic = false;
		if(TextElement.getFontPosture(font) == FontPosture.ITALIC) italic = true;

		return getFont(font.getFamily(), italic, bold, (int) (font.getSize()));
	}*/

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
		return new TextElement(getRealX(), getRealY(), getFont(), getText(), (Color) getFill(), pageNumber, page);
	}

}