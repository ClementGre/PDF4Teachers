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
import fr.themsou.render.ElementRender;
import fr.themsou.render.PDFRender;
import fr.themsou.render.PageRender;
import fr.themsou.render.TextRender;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;

@SuppressWarnings({"serial"})
public class MainScreen extends JPanel{
	
	public static int zoom = 150;
	public static File current = null;
	public static int status = 0;
	public static Image[] rendered;
	public static int page = 1;
	public static PageRender pageRender;
	public static Hand hand = null;
	private int lastWidth = getWidth();
	private int lastHeight = getHeight();
	
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
			
			MainScreen.page = -1;
			int page = 0;
			int imgsHeight = 40;
			int imgMouseX = 0;
			int imgMouseY = 0;
			for(Image img : rendered){
				
				imgMouseX = (int) (((double) (mouseX - (getWidth()/2-imgWidth/2))) / ((double)imgWidth) * img.getWidth(null));
				imgMouseY = (int) (((double) (mouseY - imgsHeight)) / ((double)imgHeight) * img.getHeight(null));
				
				Image imgRendered = pageRender.render(img, page, imgMouseX, imgMouseY);
				
				g.drawImage(imgRendered, getWidth()/2-imgWidth/2, imgsHeight, imgWidth, imgHeight, null);
				
				imgsHeight += imgHeight + 40;
				page++;
			}
			
			pageRender.afterRender(imgMouseX, imgMouseY);
			
			if(lastWidth != imgWidth || lastHeight != imgHeight){
				setPreferredSize(new Dimension(imgWidth + 80, imgsHeight));
				Main.mainScreenScroll.updateUI();
				lastWidth = imgWidth;
				lastHeight = imgHeight;
			}
			
			
			
		}
		
		
	}
	
	public void mouse(PageRender page, ElementRender element, Graphics2D g, int pageNumber, int mouseX, int mouseY){
		
		if(hand == null && Main.click && element != null){ // Ajouter
			
			hand = new Hand(element, element.getLocation().substractValues(new Location(mouseX, mouseY)), element.getPage());
			page.removeElement(element);
			
		}else if(hand != null){ // Déposer - Déplacer
			
			hand.setPage(pageNumber);
			hand.setLoc(new Location(mouseX, mouseY).additionValues(hand.getShift()));
			verifyLoc(page, hand.getElement(), mouseX, mouseY);
			
			if(!Main.click){ // Déposer
				
				page.addElement(hand.getElement());
				hand.getElement().paint(g, mouseX, mouseY);
				hand = null;
			}
		}
	}
	public void verifyLoc(PageRender page, ElementRender element, int mouseX, int mouseY){
		
		Location minLoc = element.getLocation().substractValues(element.getMargin());
		Location maxLoc = element.getLocation().additionValues(element.getMargin());
		
		if(minLoc.getX() < 0) element.setX(0 + element.getMargin().getX());
		if(maxLoc.getX() > page.getWidth()) element.setX(page.getWidth() - element.getMargin().getX());
		
		if(minLoc.getY() < 0) element.setY(0 + element.getMargin().getY());
		if(maxLoc.getY() > page.getHeight()) element.setY(page.getHeight() - element.getMargin().getY());
			
	}
	
	public void openFile(File file){
		
		closeFile();
		paintComponent(getGraphics());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		rendered = new PDFRender().render(file, 0, 4);
		if(rendered != null){
			current = file;
			status = 0;
			pageRender = new PageRender(rendered[0].getWidth(null), rendered[0].getHeight(null));
			Main.fenetre.setName("PDF Teacher - " + file.getName());
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		pageRender.addElement(new TextRender(null, 0, new Font("Arial", 0, 70), "Très grosse erreur !", new Color(172, 51, 53)));
		pageRender.addElement(new TextRender(new Location(200, 200), 0, new Font("Arial", 0, 70), "Hey !", Color.BLACK));
		pageRender.addElement(new TextRender(null, 0, new Font("Arial", 0, 70), "Bonjour.", new Color(32, 158, 16)));
		
		repaint();
		Main.footerBar.repaint();
		
	}
	public void saveFile(){
		
		
		
	}
	public void closeFile(){
		
		current = null;
		zoom = 150;
		rendered = null;
		lastWidth = 0;
		setPreferredSize(new Dimension(0, 0));
		Main.mainScreenScroll.updateUI();
		Main.fenetre.setName("PDF Teacher - Aucun document");
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
