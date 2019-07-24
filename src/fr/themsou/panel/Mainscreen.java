package fr.themsou.panel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import javax.swing.JPanel;
import fr.themsou.main.Main;
import fr.themsou.main.Render;

@SuppressWarnings({"serial"})
public class Mainscreen extends JPanel{
	
	public static int zoom = 100;
	public static File current = null;
	public static boolean loading = false;
	public static Image[] rendered;
	private int lastWidth = getWidth();
	private int lastHeight = getHeight();
	
	public void paintComponent(Graphics go){
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		System.out.println("repaint");
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		
		g.setColor(new Color(102, 102, 102));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(current == null){
			
			if(loading){
				g.setColor(Color.WHITE);
				fullCenterString(g, 0, getWidth(), 0, getHeight(), "Chargement...", new Font("FreeSans", 1, 20));
			}else{
				g.setColor(Color.WHITE);
				fullCenterString(g, 0, getWidth(), 0, getHeight(), "Aucun fichier ouvert", new Font("FreeSans", 1, 20));
			}
			
		}else{

			Image img = rendered[0];
			
			int imgWidth = 0; int imgHeight = 0;
			
			if(Main.sPaneMain.getHeight() * Main.sPaneMain.getWidth() > img.getHeight(null) * img.getWidth(null)){
				int maxSize = Main.sPaneMain.getWidth();
				imgWidth = (int) (((double)((double) zoom) / ((double) 100.0)) * ((double) maxSize) - 50);
				imgHeight = (int) (((double) imgWidth) / ((double) img.getWidth(null)) * img.getHeight(null));
			}else{
				int maxSize = Main.sPaneMain.getHeight();
				imgHeight = (int) (((double)((double) zoom) / ((double) 100.0)) * ((double) maxSize) - 50);
				imgWidth = (int) (((double) imgHeight) / ((double) img.getHeight(null)) * img.getWidth(null));
			}
			
			g.drawImage(rendered[0], getWidth() / 2 - imgWidth / 2, getHeight() / 2 - imgHeight / 2, imgWidth, imgHeight, null);
			
			
			setPreferredSize(new Dimension(imgWidth + 40, imgHeight + 40));
			Main.sPaneMain.updateUI();
			
			
			System.out.println(imgHeight + 200 + " - " + Main.sPaneMain.getHeight());
			
			
			
		}
		
		
	}
	
	public void openFile(File file){
		
		
		zoom = 100;
		rendered = null;
		current = null;
		loading = true;
		repaint();
		
		rendered = new Render().render(file);
		current = file;
		loading = false;
		repaint();
		
	}
	public void saveFile(){
		
		
		
	}
	public void closeFile(){
		
		current = null;
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
