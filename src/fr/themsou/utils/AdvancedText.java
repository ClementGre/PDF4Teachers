package fr.themsou.utils;

import java.awt.Color;
import java.awt.Font;

public class AdvancedText {
	
	private Font font = new Font("", 0, 15);
	private String content = "";
	private boolean center = false;
	private Color color = Color.BLACK;
	
	public AdvancedText(Font font, String content, Color color, boolean center) {
		super();
		this.font = font;
		this.content = content;
		this.center = center;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isCenter() {
		return center;
	}

	public void setCenter(boolean center) {
		this.center = center;
	}

}
