package fr.themsou.utils;

import fr.themsou.document.editions.elements.Element;

public class Hand {
	
	private Element element;
	private Location shift;
	
	public Hand(Element element, Location shift, int page) {
		super();
		this.element = element;
		this.shift = shift;
		this.element.setPage(page);
	}

	
	public Element getElement() {
		return element;
	}
	public void setElement(Element element) {
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
