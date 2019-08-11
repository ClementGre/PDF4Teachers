package fr.themsou.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Footerbar extends JPanel{

	public void paintComponent(Graphics go){
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
	
		if(true){
			fullCenterString(g, 0, getWidth(), 0, getHeight(), "Mode : note", new Font("FreeSans", 0, 15));
		}
		
		if(MainScreen.current != null){
			alignRightString(g, getWidth() - 4, 0, getHeight(), MainScreen.current.getName(), new Font("FreeSans", 0, 15));
		}else{
			alignRightString(g, getWidth() - 4, 0, getHeight(), "Aucun fichier ouvert", new Font("FreeSans", 0, 15));
		}
		
		alignLeftString(g, 4, 0, getHeight(), "zoom : " + MainScreen.zoom + "%", new Font("FreeSans", 0, 15));
	
	}
	
	
	public int[] alignRightString(Graphics g, int maxX, int minY, int maxY, String s, Font font){
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());
	    int rY = (int) Math.round(r2D.getY());

	    int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

	    g.setFont(font);
	    g.drawString(s, maxX - rWidth, minY + b);
	    
	    int retur[] = { rWidth, rHeight };
	    
	    return retur;
	}
	public int[] alignLeftString(Graphics g, int minX, int minY, int maxY, String s, Font font) {
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());
	    int rY = (int) Math.round(r2D.getY());

	    int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

	    g.setFont(font);
	    g.drawString(s, minX, minY + b);
	    
	    int retur[] = { rWidth, rHeight };
	    
	    return retur;
	}
	
	public int[] fullCenterString(Graphics g, int minX, int maxX, int minY, int maxY, String s, Font font) {
		
		
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
