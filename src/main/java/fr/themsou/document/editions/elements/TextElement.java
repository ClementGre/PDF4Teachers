package fr.themsou.document.editions.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fr.themsou.utils.Location;

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
	public boolean paint(Graphics2D g, int mouseX, int mouseY){


		g.setColor(color);
		int[] dim = fullCenterString(g, getX(), getX(), getY(), getY(), content, new Font(font.getName(), font.getStyle(), (int) (font.getSize() * 2.75)));
		setMargin(new Location(dim[0] / 2 + 10, dim[1] / 2 + 10));
		
		if(mouseX > (getX() - dim[0]/2) && mouseX < (getX() + dim[0]/2)){
			if(mouseY > (getY() - dim[1]/2) && mouseY < (getY() + dim[1]/2)){
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
		writer.writeByte(font.getSize());
		writer.writeByte(font.getStyle());
		writer.writeUTF(font.getFontName());
		writer.writeByte(color.getRed() - 128);
		writer.writeByte(color.getGreen() - 128);
		writer.writeByte(color.getBlue() - 128);
		writer.writeUTF(content);
	}

	public static Element readDataAndCreate(DataInputStream reader) throws IOException{

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		byte fontSize = reader.readByte();
		byte fontStyle = reader.readByte();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String content = reader.readUTF();

		return new TextElement(new Location(x + 30, y + 30), page, new Font(fontName, fontStyle, fontSize), content, new Color(colorRed, colorGreen, colorBlue));
	}

}
