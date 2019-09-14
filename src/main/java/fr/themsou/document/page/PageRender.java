package fr.themsou.document.page;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import fr.themsou.document.elements.Element;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import fr.themsou.utils.Location;

public class PageRender {
	
	
	private ArrayList<Element> elements = new ArrayList<>();

	private int width;
	private int height;
	private Element current = null;
	
	public PageRender(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	
	public Image render(Image img, int page, int mouseX, int mouseY){
		
		BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null), null);


		for(int i = 0; i < elements.size(); i++){
			if(elements.get(i).getPage() == page){
				
				boolean on = elements.get(i).paint(g, mouseX, mouseY);
				if(on){
					current = elements.get(i);
				}
			}
		}
		if(!((mouseY > img.getHeight(null) && (page+1) != MainScreen.rendered.length) || (mouseY < 0 && page != 0))){ // mouse on

			Main.mainScreen.mouse(this, current, g, page, mouseX, mouseY);
			if(MainScreen.hand != null)
				MainScreen.hand.getElement().paint(g, mouseX, mouseY);
			if(MainScreen.page != page){
				MainScreen.page = page;
				Main.footerBar.repaint();
			}
		}
		
		g.dispose();
		return (Image) bimg;
		
	}
	public void afterRender(int mouseX, int mouseY){
		
		if(current != null || MainScreen.hand != null){
			if(Main.mainScreen.getCursor() != Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
				Main.mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}else{
			if(Main.mainScreen.getCursor() != Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
				Main.mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		current = null;
		
		if(MainScreen.page == -1) Main.footerBar.repaint();
		
	}
	
	
	
	public void addElement(Element element){
		
		if(element != null){	
			
			if(element.getLocation() == null){
				element.setLocation(new Location(width / 2, height / 2));
			}
			elements.add(element);
		}
	}
	public void removeElement(Element element){
		
		if(element != null){
			
			elements.remove(element);
		}
	}


	public int getHeight(){
		return height;
	}
	public int getWidth(){
		return width;
	}

}
