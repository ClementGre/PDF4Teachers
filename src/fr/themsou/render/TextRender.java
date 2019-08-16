package fr.themsou.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import fr.themsou.utils.Location;

public class TextRender extends ElementRender{
	
	private Font font = new Font("", 0, 15);
	private String content = "";
	private Color color = Color.BLACK;
	
	public TextRender(Location loc, int page, Font font, String content, Color color){
		super(loc, page);
		this.font = font;
		this.content = content;
		this.color = color;
	}

	public Color getColor(){
		return color;
	}public void setColor(Color color){
		this.color = color;
	}public Font getFont(){
		return font;
	}public void setFont(Font font){
		this.font = font;
	}public String getContent(){
		return content;
	}public void setContent(String content){
		this.content = content;
	}

	@Override
	public boolean paint(Graphics2D g, int mouseX, int mouseY){
		
		g.setColor(color);
		
		int[] dim = fullCenterString(g, getX(), getX(), getY(), getY(), content, font);
		setMargin(new Location(dim[0] / 2 + 10, dim[1] / 2 + 10));
		
		if(mouseX > (getX() - dim[0]/2) && mouseX < (getX() + dim[0]/2)){
			if(mouseY > (getY() - dim[1]/2) && mouseY < (getY() + dim[1]/2)){
				return true;
			}
		}
		return false;
		
	}

	@Override
	public boolean equals(ElementRender element){
		
		if(element instanceof TextRender){
			
			TextRender text = (TextRender) element;
			
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

}
