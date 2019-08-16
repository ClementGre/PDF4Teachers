package fr.themsou.utils;

import fr.themsou.render.ElementRender;

public class Hand {
	
	private ElementRender element;
	private Location shift;
	
	public Hand(ElementRender element, Location shift, int page) {
		super();
		this.element = element;
		this.shift = shift;
		this.element.setPage(page);
	}

	
	public ElementRender getElement() {
		return element;
	}
	public void setElement(ElementRender element) {
		this.element = element;
	}
	public Location getLoc() {
		return element.getLocation();
	}

	public void setLoc(Location loc) {
		this.element.setLocation(loc);
	}

	public Location getShift() {
		return shift;
	}

	public void setShift(Location shift) {
		this.shift = shift;
	}

	public int getPage() {
		return element.getPage();
	}

	public void setPage(int page) {
		this.element.setPage(page);
	}
	
	

}
