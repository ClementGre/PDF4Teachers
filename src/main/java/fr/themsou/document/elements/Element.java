package fr.themsou.document.elements;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import fr.themsou.utils.Location;

public abstract class Element {
	
	private Location loc;
	private Location margin;
	private int page;
	
	Element(Location loc, int page) {
		this.loc = loc;
		this.page = page;
	}

	public abstract boolean paint(Graphics2D g, int mouseX, int mouseY);
	public abstract boolean equals(Element object);


	public Element setLocation(Location loc){
		this.loc = loc;
		return this;
	}
	public Location getLocation(){
		return loc;
	}
	
	int getX(){
		return loc.getX();
	}
	public int getY(){
		return loc.getY();
	}
	public Element setX(int x){
		this.loc.setX(x);
		return this;
	}
	public Element setY(int y){
		this.loc.setY(y);
		return this;
	}
	public Location getMargin(){
		return margin;
	}
	public void setMargin(Location margin){
		this.margin = margin;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	@Override
	public String toString() {
		return "Element [X=" + getX() + ", Y=" + getY() + ", SUB=" + super.toString() + "]";
	}
	
	public static int[] getStringDimensions(String s, Font font) {
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());

	    int retur[] = { rWidth, rHeight };
	    return retur;

	    
	}
	public static int[] fullCenterString(Graphics g, int minX, int maxX, int minY, int maxY, String s, Font font) {
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());
	    int rX = (int) Math.round(r2D.getX());
	    int rY = (int) Math.round(r2D.getY());

	    int a = ((maxX - minX) / 2) - (rWidth / 2) - rX;
	    int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

	    g.setFont(font);
	    g.drawString(s, minX + a, minY + b);
	    
	    int retur[] = { rWidth, rHeight };
	    return retur;
	}
}
