package fr.themsou.document.editions.elements;

import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NoDisplayTextElement extends TreeItem{

	private Font font;
	private String text;
	private Color color;

	private int type;
	private long uses;
	private long creationDate;

	private TextElement core = null;

	public static final int FAVORITE_TYPE = 1;
	public static final int LAST_TYPE = 2;
	public static final int ONFILE_TYPE = 3;

	public NoDisplayTextElement(Font font, String text, Color color, int type, long uses, long creationDate) {
		this.font = font;
		this.text = text;
		this.color = color;
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;
	}
	public NoDisplayTextElement(Font font, String text, Color color, int type, long uses, long creationDate, TextElement core) {
		this.font = font;
		this.text = text;
		this.color = color;
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;
		this.core = core;

		core.textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				setText(newValue);
				Main.lbTextTab.treeView.refresh();
			}
		});
		core.fontProperty().addListener(new ChangeListener<Font>() {
			@Override public void changed(ObservableValue<? extends Font> observable, Font oldValue, Font newValue){
				setFont(newValue);
				Main.lbTextTab.treeView.refresh();
			}
		});
		core.fillProperty().addListener(new ChangeListener<Paint>() {
			@Override public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
				setColor((Color) newValue);
				Main.lbTextTab.treeView.refresh();
			}
		});
		if(type == NoDisplayTextElement.ONFILE_TYPE){
			core.setOnMouseReleased(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					if(event.getButton() == MouseButton.PRIMARY){
						if(Main.lbTextTab.onFileTextSortManager.getSelectedButton().getText().equals("Position")){
							Main.lbTextTab.onFileTextSortManager.simulateCall();
						}
					}
				}
			});
		}

	}

	@Override
	public boolean equals(Object v){

		if(v instanceof NoDisplayTextElement){
			NoDisplayTextElement element = (NoDisplayTextElement) v;
			if(element.type == type && element.text.equals(text) && element.color.hashCode() == color.hashCode()){
				if(element.font.getStyle().equals(font.getStyle()) && element.font.getSize() == font.getSize() && element.getFont().getFamily().equals(font.getFamily())){
					return true;
				}
			}
		}
		return false;
	}

	public void writeData(DataOutputStream writer) throws IOException {

		writer.writeFloat((float) font.getSize());
		writer.writeBoolean(TextElement.getFontWeight(font) == FontWeight.BOLD);
		writer.writeBoolean(TextElement.getFontPosture(font) == FontPosture.ITALIC);
		writer.writeUTF(font.getFamily());
		writer.writeByte((int) (color.getRed() * 255.0 - 128));
		writer.writeByte((int) (color.getGreen() * 255.0 - 128));
		writer.writeByte((int) (color.getBlue() * 255.0 - 128));
		writer.writeLong(uses);
		writer.writeLong(creationDate);
		writer.writeUTF(text);
	}
	public TextElement toRealTextElement(int x, int y, int page){
		return new TextElement(x, y, font, text, color, page, Main.mainScreen.document.pages.get(page));
	}
	public static NoDisplayTextElement readDataAndGive(DataInputStream reader, int type) throws IOException {

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

		Font font = TextElement.getFont(fontName, isBold, isItalic, (int) fontSize);

		return new NoDisplayTextElement(font, text, Color.rgb(colorRed, colorGreen, colorBlue), type, uses, creationDate);

	}

	public void addToDocument(){

		if(Main.mainScreen.hasDocument(false)){
			uses++;
			PageRenderer page = Main.mainScreen.document.pages.get(0);
			if (Main.mainScreen.document.getCurrentPage() != -1)
				page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

			TextElement realElement = toRealTextElement(30, (int) (page.mouseY * 800 / page.getHeight()), page.getPage());
			page.addElement(realElement, true);
			Main.mainScreen.selectedProperty().setValue(realElement);
		}

	}

	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
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
	public void setCore(TextElement core) {
		this.core = core;
	}
}