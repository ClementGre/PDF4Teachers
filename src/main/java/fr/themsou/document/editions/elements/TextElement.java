package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class TextElement extends Text implements Element {

	private IntegerProperty realX = new SimpleIntegerProperty();
	private IntegerProperty realY = new SimpleIntegerProperty();

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

		layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
		layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

		setCursor(Cursor.MOVE);

		// enable shadow if this element is selected
		MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
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
			PageRenderer page1 = MainWindow.mainScreen.document.pages.get(0);
			if (MainWindow.mainScreen.document.getCurrentPage() != -1)
				page1 = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

			TextElement realElement = (TextElement) this.clone();
			realElement.setRealX(realElement.getRealX() + 10);
			realElement.setRealY(realElement.getRealY() + 10);
			page1.addElement(realElement, true);
			MainWindow.mainScreen.selectedProperty().setValue(realElement);
		});
		item3.setOnAction(e -> MainWindow.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.LAST_TYPE, true)));
		item4.setOnAction(e -> MainWindow.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.FAVORITE_TYPE, true)));

		setOnMousePressed(e -> {
			e.consume();

			shiftX = (int) e.getX();
			shiftY = (int) e.getY();
			menu.hide();
			select();

			if(e.getButton() == MouseButton.SECONDARY){
				menu.show(getPage(), e.getScreenX(), e.getScreenY());
			}
		});

		setOnMouseDragged(e -> {

			Edition.setUnsave();
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			boolean changePage = false;
			if(getPage().getRealMouseY() < -30){
				if(getPageNumber() > 0){

					MainWindow.mainScreen.setSelected(null);
					switchPage(getPageNumber() -1);

					itemY = getPage().getHeight();
					changePage = true;
				}
			}else if(getPage().getRealMouseY() > getPage().getHeight() + 30){
				if(getPageNumber() < MainWindow.mainScreen.document.pages.size()-1){

					MainWindow.mainScreen.setSelected(null);
					switchPage(getPageNumber() +1);

					itemY = 0;
					changePage = true;
				}
			}

			checkLocation(itemX, itemY);

			if(changePage){
				layoutXProperty().bind(getPage().widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
				layoutYProperty().bind(getPage().heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));
				MainWindow.lbTextTab.onFileTextSortManager.simulateCall();
			}

		});

		textProperty().addListener((observable, oldValue, newValue) -> {
			Edition.setUnsave();

			if(getLayoutY() < getLayoutBounds().getHeight()){
				checkLocation(getLayoutX(), getLayoutY());
			}
		});
	}

	// CHECK LOCATION

	@Override
	public void checkLocation(double itemX, double itemY){

		setBoundsType(TextBoundsType.VISUAL);
		double linesHeight = getLayoutBounds().getHeight();
		if(itemY < linesHeight) itemY = linesHeight;
		if(itemY > getPage().getHeight()) itemY = getPage().getHeight();
		if(itemX < 0) itemX = 0;
		if(itemX > getPage().getWidth() - getLayoutBounds().getWidth()) itemX = getPage().getWidth() - getLayoutBounds().getWidth();
		setBoundsType(TextBoundsType.LOGICAL);

		realX.set((int) (itemX / getPage().getWidth() * Element.GRID_WIDTH));
		realY.set((int) (itemY / getPage().getHeight() * Element.GRID_HEIGHT));

	}

	// SELECT - DELETE - SWITCH PAGE

	@Override
	public void select() {

		MainWindow.leftBar.getSelectionModel().select(1);
		MainWindow.mainScreen.setSelected(this);
		MainWindow.lbTextTab.selectItem();
		toFront();
	}
	@Override
	public void delete() {
		getPage().removeElement(this, true);

		/*if(getPage() != null){
			if(getPage().getChildren().contains(this)) getPage().removeElement(this, true);
		}*/
	}
	@Override
	public void switchPage(int page){
		getPage().switchElementPage(this, MainWindow.mainScreen.document.pages.get(page));
	}

	// READER AND WRITERS

	@Override
	public void writeSimpleData(DataOutputStream writer) throws IOException {
		writer.writeByte(1);
		writeData(writer);
	}
	@Override
	public void writeData(DataOutputStream writer) throws IOException {
		writer.writeByte(getPageNumber());
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
		return new TextElement(x, y, font, text, Color.rgb(colorRed, colorGreen, colorBlue), page, hasPage ? MainWindow.mainScreen.document.pages.get(page) : null);

	}
	public static void readDataAndCreate(DataInputStream reader) throws IOException {
		TextElement element = readDataAndGive(reader, true);
		if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
			MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
	}
	public static void consumeData(DataInputStream reader) throws IOException {
		reader.readByte();reader.readShort();reader.readShort();
		reader.readFloat();reader.readBoolean();reader.readBoolean();reader.readUTF();
		reader.readByte();reader.readByte();reader.readByte();
		reader.readUTF();
	}

	// COORDINATES GETTERS ANS SETTERS

	@Override
	public int getRealX() {
		return realX.get();
	}
	@Override
	public IntegerProperty RealXProperty() {
		return realX;
	}
	@Override
	public void setRealX(int x) {
		this.realX.set(x);
	}
	@Override
	public int getRealY() {
		return realY.get();
	}
	@Override
	public IntegerProperty RealYProperty() {
		return realY;
	}
	@Override
	public void setRealY(int y) {
		this.realY.set(y);
	}

	// PAGE GETTERS ANS SETTERS

	@Override
	public PageRenderer getPage() {
		if(MainWindow.mainScreen.document.pages.size() > pageNumber){
			return MainWindow.mainScreen.document.pages.get(pageNumber);
		}
		return null;
	}
	@Override
	public int getPageNumber() {
		return pageNumber;
	}
	@Override
	public void setPage(PageRenderer page) {
		this.pageNumber = page.getPage();
	}
	@Override
	public void setPage(int pageNumber){
		this.pageNumber = pageNumber;
	}

	// TRANSFORMATIONS

	@Override
	public Element clone() {
		return new TextElement(getRealX(), getRealY(), getFont(), getText(), (Color) getFill(), pageNumber, getPage());
	}
	public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
		if(hasCore) return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000);
	}

}