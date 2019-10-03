package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fr.themsou.utils.Location;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TextElement extends Element{
	
	private Font font;
	private String content;
	private Color color;
	
	public TextElement(Location loc, int page, Font font, String content, Color color){
		super(loc, page);
		this.font = font;
		this.content = content;
		this.color = color;

	}

	public Color getColor(){
		return color;
	}
	public void setColor(Color color){
		this.color = color;
	}
	public Font getFont(){
		return font;
	}
	public void setFont(Font font){
		this.font = font;
	}
	public String getContent(){
		return content;
	}
	public void setContent(String content){
		this.content = content;
	}

	@Override
	public boolean paint(GraphicsContext g, int mouseX, int mouseY){


		g.setFill(color);
		g.setTextAlign(TextAlignment.CENTER);
		g.setTextBaseline(VPos.CENTER);


		final Text text = new Text(content);
		Font font = Font.font("Arial", 20);
		text.setFont(new Font(font.getName(), (int) (font.getSize() * 2.75)));

		final double width = text.getLayoutBounds().getWidth();
		final double height = text.getLayoutBounds().getHeight();



		setMargin(new Location((int) width / 2 + 10, (int) height / 2 + 10));
		
		if(mouseX > (getX() - width/2) && mouseX < (getX() + width/2)){
			if(mouseY > (getY() - height/2) && mouseY < (getY() + height/2)){
				return true;
			}
		}
		return false;
		
	}

	@Override
	public boolean equals(Element element){
		
		if(element instanceof TextElement){
			
			TextElement text = (TextElement) element;
			
			if(content.equals(text.getContent())){
				if(font.getName().equals(text.getFont().getName()) && font.getSize() == text.getFont().getSize()){
					if(color.equals(text.getColor())){
						return true;
					}
				}
			}
		}
		
		return false;
		
	}

	@Override
	public void writeData(DataOutputStream writer) throws IOException{
		writer.writeByte(1);
		writer.writeByte(super.page);
		writer.writeShort(getX());
		writer.writeShort(getY());
		writer.writeByte((int) font.getSize());
		writer.writeUTF(font.getName());
		writer.writeByte((int) color.getRed() - 128);
		writer.writeByte((int) color.getGreen() - 128);
		writer.writeByte((int) color.getBlue() - 128);
		writer.writeUTF(content);
	}

	public static Element readDataAndCreate(DataInputStream reader) throws IOException{

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		byte fontSize = reader.readByte();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String content = reader.readUTF();

		return new TextElement(new Location(x + 30, y + 30), page, new Font(fontName, fontSize), content, Color.rgb(colorRed, colorGreen, colorBlue));
	}

}
