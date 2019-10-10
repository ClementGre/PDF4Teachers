package fr.themsou.document.editions.elements;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextElement extends Label implements Element {

	private IntegerProperty x = new SimpleIntegerProperty();
	private IntegerProperty y = new SimpleIntegerProperty();
	private PageRenderer page;
	private int shiftX = 0;
	private int shiftY = 0;
	Thread moover;

	public TextElement(int x, int y, Font font, String text, Color color, PageRenderer page){

		setLayoutX(x);
		setLayoutY(y);

		this.x.bind(layoutXProperty().divide(page.widthProperty()).multiply(100));
		this.y.bind(layoutYProperty().divide(page.heightProperty()).multiply(100));

		setFont(font);
		setText(text);
		setTextFill(color);

		setCursor(Cursor.MOVE);

		if(page != null)
			this.page = page;

		setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {

				shiftX = (int) (getLayoutX() - MouseInfo.getPointerInfo().getLocation().getX());
				shiftY = (int) (getLayoutY() - MouseInfo.getPointerInfo().getLocation().getY());

				if(moover == null){
					moover = new Thread(new Runnable() {
						@Override public void run(){
							while(true){
								try{
									Thread.sleep(30);
								}catch(InterruptedException e){ e.printStackTrace(); }

								int x = (int) (MouseInfo.getPointerInfo().getLocation().getX() + shiftX);
								int y = (int) (MouseInfo.getPointerInfo().getLocation().getY() + shiftY);

								if(x < 0) x = 0;
								if(x > page.getWidth() - getLayoutBounds().getWidth()) x = (int) (page.getWidth() - getLayoutBounds().getWidth());
								if(y < 0) y = 0;
								if(y > page.getHeight() - getLayoutBounds().getHeight()) y = (int) (page.getHeight() - getLayoutBounds().getHeight());

								setLayoutX(x);
								setLayoutY(y);
							}
						}
					}, "moover");
					moover.start();
				}else{
					moover.stop();
					moover = null;
				}
				select();
			}
		});
		setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(moover != null){
					moover.stop();
					moover = null;
				}
			}
		});
	}
	void select(){
		Main.mainScreen.document.selected = this;
		toFront();
	}
	@Override
	public void delete(){
		page.removeElement(this);
	}
	public void writeData(DataOutputStream writer) throws IOException{
		writer.writeByte(1);
		writer.writeByte(page.getPage());
		writer.writeShort(getX());
		writer.writeShort(getY());
		writer.writeByte((int) getFont().getSize());
		writer.writeUTF(getFont().getName());
		writer.writeByte((int) ((Color)getTextFill()).getRed() - 128);
		writer.writeByte((int) ((Color)getTextFill()).getGreen() - 128);
		writer.writeByte((int) ((Color)getTextFill()).getBlue() - 128);
		writer.writeUTF(getText());
	}
	public static void readDataAndCreate(DataInputStream reader) throws IOException{

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		byte fontSize = reader.readByte();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String text = reader.readUTF();

		if(Main.mainScreen.document.pages.size() > page){
			Main.mainScreen.document.pages.get(page).addElement(
					new TextElement(
							x, y, new Font(fontName, fontSize), text, Color.rgb(colorRed, colorGreen, colorBlue), Main.mainScreen.document.pages.get(page)));
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
}
