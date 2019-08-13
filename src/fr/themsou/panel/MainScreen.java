package fr.themsou.panel;

import java.awt.Color;
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
import java.util.HashMap;

import javax.swing.JPanel;

import fr.themsou.main.Main;
import fr.themsou.render.EditRender;
import fr.themsou.render.PDFRender;
import fr.themsou.utils.AdvancedText;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;

@SuppressWarnings({"serial"})
public class MainScreen extends JPanel{
	
	public static int zoom = 150;
	public static File current = null;
	public static int status = 0;
	public static Image[] rendered;
	public static int pages = 0;
	public static HashMap<Integer, EditRender> pagesRenderer = new HashMap<>();
	public static Hand hand = null;
	private int lastWidth = getWidth();
	private int lastHeight = getHeight();
	
	@SuppressWarnings("static-access")
	public void paintComponent(Graphics go){
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		
		g.setColor(new Color(102, 102, 102));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(current == null){
			
			if(status == 0){
				g.setColor(Color.WHITE);
				fullCenterString(g, 0, getWidth(), 0, getHeight(), "Aucun document ouvert", new Font("FreeSans", 1, 20));
			}else if(status == 1){
				g.setColor(Color.WHITE);
				fullCenterString(g, 0, getWidth(), 0, getHeight(), "Chargement du document...", new Font("FreeSans", 1, 20));
			}else if(status == 2){
				g.setColor(Color.WHITE);
				fullCenterString(g, 0, getWidth(), 0, getHeight(), "Une erreur est survenue lors du chargement du document :/", new Font("FreeSans", 1, 20));
			}
			
		}else{

			int imgWidth = 0; int imgHeight = 0;
			if((double)Main.mainScreenScroll.getHeight() / (double)Main.mainScreenScroll.getWidth() > (double)rendered[0].getHeight(null) / (double)rendered[0].getWidth(null)){
				int maxSize = Main.mainScreenScroll.getWidth();
				imgWidth = (int) (((double)((double) zoom) / ((double) 100.0)) * ((double) maxSize) -100);
				imgHeight = (int) (((double) imgWidth) / ((double) rendered[0].getWidth(null)) * rendered[0].getHeight(null));
			}else{
				int maxSize = Main.mainScreenScroll.getHeight();
				imgHeight = (int) (((double)((double) zoom) / ((double) 100.0)) * ((double) maxSize) -100);
				imgWidth = (int) (((double) imgHeight) / ((double) rendered[0].getHeight(null)) * rendered[0].getWidth(null));
			}
			
			int pages = 0;
			int imgsHeight = 40;
			for(Image img : rendered){
				
				g.drawImage(img, getWidth()/2-imgWidth/2, imgsHeight, imgWidth, imgHeight, null);
				setCursor(pagesRenderer.get(pages).render(g, getWidth()/2-imgWidth/2, imgsHeight, imgWidth, imgHeight, mouseX, mouseY));
				
				imgsHeight += imgHeight + 40;
				pages++;
			}
			this.pages = pages;
			
			
			
			if(lastWidth != imgWidth || lastHeight != imgHeight){
				setPreferredSize(new Dimension(imgWidth + 80, imgsHeight));
				Main.mainScreenScroll.updateUI();
				lastWidth = imgWidth;
				lastHeight = imgHeight;
			}
			
			
			
		}
		
		
	}
	
	public void openFile(File file){
		
		zoom = 150;
		rendered = null;
		current = null;
		status = 1;
		lastWidth = 0;
		setPreferredSize(new Dimension(0, 0));
		Main.mainScreenScroll.updateUI();
		paintComponent(getGraphics());
		
		rendered = new PDFRender().render(file, 0, 4);
		if(rendered != null){
			current = file;
			status = 0;
			
			for(int i = 0; i < rendered.length; i++){
				pagesRenderer.put(i, new EditRender());
			}
		}
		
		pagesRenderer.get(0).putText(new AdvancedText(new Font("Arial", 0, 10), "Très grosse erreur !", Color.RED, false), new Location(50, 50));
		
		repaint();
		Main.footerBar.repaint();
		
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
