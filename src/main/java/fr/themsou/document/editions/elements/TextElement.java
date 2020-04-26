package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class TextElement extends Text implements Element {

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
		setTextOrigin(VPos.BASELINE);
		setFill(color);

		setStyle("-fx-background-color: black;");

		setBoundsType(TextBoundsType.LOGICAL);

		if(page == null) return;
		this.page = page;

		layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
		layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

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
		item3.setOnAction(e -> Main.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.LAST_TYPE, true)));
		item4.setOnAction(e -> Main.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.FAVORITE_TYPE, true)));

		setOnMousePressed(e -> {
			e.consume();

			shiftX = (int) e.getX();
			shiftY = (int) e.getY();
			menu.hide();
			select();

			if(e.getButton() == MouseButton.SECONDARY){
				menu.show(this.page, e.getScreenX(), e.getScreenY());
			}
		});

		setOnMouseDragged(e -> {

			Edition.setUnsave();
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			boolean changePage = false;
			if(this.page.getMouseY() < -30){
				if(this.page.getPage() > 0){

					Main.mainScreen.setSelected(null);

					this.page.removeElement(this, false);
					this.page = Main.mainScreen.document.pages.get(this.page.getPage() -1);
					this.page.addElement(this, false);

					itemY = this.page.getHeight();
					changePage = true;
				}
			}else if(this.page.getMouseY() > this.page.getHeight() + 30){
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
				layoutXProperty().bind(this.page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
				layoutYProperty().bind(this.page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));
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

		setBoundsType(TextBoundsType.VISUAL);
		double linesHeight = getLayoutBounds().getHeight();
		if(itemY < linesHeight) itemY = linesHeight;
		if(itemY > page.getHeight()) itemY = page.getHeight();
		if(itemX < 0) itemX = 0;
		if(itemX > page.getWidth() - getLayoutBounds().getWidth()) itemX = page.getWidth() - getLayoutBounds().getWidth();
		setBoundsType(TextBoundsType.LOGICAL);

		realX.set((int) (itemX / page.getWidth() * Element.GRID_WIDTH));
		realY.set((int) (itemY / page.getHeight() * Element.GRID_HEIGHT));

	}

	@Override
	public void select() {

		Main.leftBar.getSelectionModel().select(1);
		Main.mainScreen.setSelected(this);
		Main.lbTextTab.selectItem();
		toFront();
	}

	public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
		if(hasCore) return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000);
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
		writer.writeBoolean(Element.getFontWeight(getFont()) == FontWeight.BOLD);
		writer.writeBoolean(Element.getFontPosture(getFont()) == FontPosture.ITALIC);
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

		Font font = Element.getFont(fontName, isItalic, isBold, (int) fontSize);

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

	public PageRenderer getPage() {
		return page;
	}
	public void setPage(PageRenderer page) {
		this.page = page;
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