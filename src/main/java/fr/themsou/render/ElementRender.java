package fr.themsou.render;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import fr.themsou.utils.Location;

public abstract class ElementRender {
	
	private Location loc;
	private Location margin;
	private int page;
	
	public ElementRender(Location loc, int page) {
		this.loc = loc;
		this.page = page;
	}

	public abstract boolean paint(Graphics2D g, int mouseX, int mouseY);
	public abstract boolean equals(ElementRender object);

	
	public ElementRender setLocation(Location loc){
		this.loc = loc;
		return this;
	}
	public Location getLocation(){
		return loc;
	}
	
	public int getX(){
		return loc.getX();
	}
	public int getY(){
		return loc.getY();
	}
	public ElementRender setX(int x){
		this.loc.setX(x);
		return this;
	}
	public ElementRender setY(int y){
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
		return "ElementRender [X=" + getX() + ", Y=" + getY() + ", SUB=" + super.toString() + "]";
	}
	
	public static int[] getStringDimensions(Graphics2D g, int minX, int maxX, int minY, int maxY, String s, Font font) {
		
		
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
