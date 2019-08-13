package fr.themsou.utils;

import java.awt.Image;

public class Hand {
	
	private AdvancedText text = null;
	private Image draw = null;
	private int note = -1;
	private Location shift = null;
	
	public Hand(AdvancedText text, Image draw, int note, Location shift) {
		super();
		this.text = text;
		this.draw = draw;
		this.note = note;
		this.shift = shift;
	}
	
	public int getType() {
		if(text != null){
			return 1;
		}
		if(draw != null){
			return 2;
		}
		if(note != 2){
			return 3;
		}
		return 0;
	}
	
	public AdvancedText getText() {
		return text;
	}

	public void setText(AdvancedText text) {
		this.text = text;
	}

	public Image getDraw() {
		return draw;
	}

	public void setDraw(Image draw) {
		this.draw = draw;
	}

	public int getNote() {
		return note;
	}

	public void setNote(int note) {
		this.note = note;
	}

	public Location getShift() {
		return shift;
	}

	public void setShift(Location shift) {
		this.shift = shift;
	}
	

}
