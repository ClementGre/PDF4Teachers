package fr.themsou.document.editions.elements;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class TextElement extends Label implements Element {

	private IntegerProperty x = new SimpleIntegerProperty();
	private IntegerProperty y = new SimpleIntegerProperty();
	private PageRenderer page;
	private ObjectProperty<Font> realFont = new SimpleObjectProperty<>();

	private int shiftX = 0;
	private int shiftY = 0;
	Thread moover;

	public TextElement(int x, int y, Font font, String text, Color color, PageRenderer page) {

		this.x.set(x);
		this.y.set(y);

		layoutXProperty().bind(page.widthProperty().multiply(this.x.divide(500.0)));
		layoutYProperty().bind(page.heightProperty().multiply(this.y.divide(800.0)));

		setRealFont(font);
		setText(text);
		setStyle("-fx-text-fill: #" + Integer.toHexString(color.hashCode()));
		setTextFill(color);

		fontProperty().bind(Bindings.createObjectBinding(() -> {
			return translateFont(getRealFont());
		}, realFontProperty(), Main.mainScreen.zoomProperty()));

		setCursor(Cursor.MOVE);

		if (page != null)
			this.page = page;


		setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override public void handle(MouseEvent e){
				shiftX = (int) e.getX();
				shiftY = (int) e.getY();
				select();
			}
		});

		TextElement thisObject = this;
		setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {

				shiftX = 0;
				shiftY = 0;

				double itemX = thisObject.page.mouseX - shiftX;
				double itemY = thisObject.page.mouseY - shiftY;

				if(thisObject.page.mouseY < -30){
					if(thisObject.page.getPage() > 0){

						thisObject.page.removeElement(thisObject);
						thisObject.page = Main.mainScreen.document.pages.get(thisObject.page.getPage() -1);
						thisObject.page.addElement(thisObject);

						itemY = thisObject.page.getHeight() - getLayoutBounds().getHeight();
					}
				}else if(thisObject.page.mouseY > thisObject.page.getHeight() + 30){
					if(thisObject.page.getPage() < Main.mainScreen.document.pages.size()-1){

						thisObject.page.removeElement(thisObject);
						thisObject.page = Main.mainScreen.document.pages.get(thisObject.page.getPage() + 1);
						thisObject.page.addElement(thisObject);

						itemY = 0;
					}
				}

				if(itemY < 0) itemY = 0;
				if(itemY > thisObject.page.getHeight() - getLayoutBounds().getHeight()) itemY = thisObject.page.getHeight() - getLayoutBounds().getHeight();
				if(itemX < 0) itemX = 0;
				if(itemX > thisObject.page.getWidth() - getLayoutBounds().getWidth()) itemX = thisObject.page.getWidth() - getLayoutBounds().getWidth();

				thisObject.x.set((int) (itemX / thisObject.page.getWidth() * 500.0));
				thisObject.y.set((int) (itemY / thisObject.page.getHeight() * 800.0));

			}
		});
	}

	void select() {

		Main.mainScreen.setSelected(this);
		toFront();
	}

	@Override
	public void delete() {
		page.removeElement(this);
	}

	public void writeData(DataOutputStream writer) throws IOException {
		writer.writeByte(1);
		writer.writeByte(page.getPage());
		writer.writeShort(getX());
		writer.writeShort(getY());
		writer.writeFloat((float) getRealFont().getSize());
		writer.writeBoolean(getFontWeight(getRealFont()) == FontWeight.BOLD);
		writer.writeBoolean(getFontPosture(getRealFont()) == FontPosture.ITALIC);
		writer.writeUTF(getRealFont().getFamily());
		writer.writeByte((int) (((Color) getTextFill()).getRed() * 255.0 - 128));
		writer.writeByte((int) (((Color) getTextFill()).getGreen() * 255.0 - 128));
		writer.writeByte((int) (((Color) getTextFill()).getBlue() * 255.0 - 128));
		writer.writeUTF(getText());
	}

	public static void readDataAndCreate(DataInputStream reader) throws IOException {

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

		FontWeight fontWeight = isBold ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture fontPosture = isItalic ? FontPosture.ITALIC : FontPosture.REGULAR;
		Font font = Font.font(fontName, fontWeight, fontPosture, fontSize);

		if (Main.mainScreen.document.pages.size() > page) {
			Main.mainScreen.document.pages.get(page).addElement(
					new TextElement(x, y, font, text, Color.rgb(colorRed, colorGreen, colorBlue), Main.mainScreen.document.pages.get(page)));
		}
	}



	public int getX() {
		return x.get();
	}

	public IntegerProperty xProperty() {
		return x;
	}

	public void setX(int x) {
		this.x.set(x);
	}

	public int getY() {
		return y.get();
	}

	public IntegerProperty yProperty() {
		return y;
	}

	public void setY(int y) {
		this.y.set(y);
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
		return Font.font(font.getFamily(), getFontWeight(font), getFontPosture(font), font.getSize() / 75.0 * Main.mainScreen.getZoom());
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
}