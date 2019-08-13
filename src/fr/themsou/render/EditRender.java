package fr.themsou.render;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import fr.themsou.utils.AdvancedText;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;

public class EditRender {
	
	
	public HashMap<Location, AdvancedText> texts = new HashMap<>();
	public HashMap<Location, Image> draws = new HashMap<>();
	public HashMap<Location, Integer> notes = new HashMap<>();
	
	public Cursor render(Graphics2D g, int startX, int startY, int width, int height, int mouseX, int mouseY){
		
		Cursor cursor = Cursor.getDefaultCursor();
		checkClick(false, null, g, startX, startY, width, height, mouseX, mouseY);
		
		for(Map.Entry<Location, AdvancedText> entry : texts.entrySet()){
			
			Location loc = getLocationPX(entry.getKey().getX(), entry.getKey().getY(), startX, startY, width, height);
			
			if(drawText(entry.getValue(), loc, g, startX, startY, width, height, mouseX, mouseY)){
				cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				checkClick(true, new Hand(entry.getValue(), null, -1, new Location(loc.getX() - mouseX + 1, loc.getY() - mouseY + 1)), g, startX, startY, width, height, mouseX, mouseY);
			}
		}
		
		if(MainScreen.hand != null) cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		
		return cursor;
		
	}
	
	public void checkClick(boolean onItem, Hand hand, Graphics2D g, int startX, int startY, int width, int height, int mouseX, int mouseY){
		
		if(Main.click && MainScreen.hand == null && onItem){ // add
			
			MainScreen.hand = hand;
			Location loc = getLocationPercent(mouseX + MainScreen.hand.getShift().getX(), mouseY + MainScreen.hand.getShift().getY(), startX, startY, width, height);
			
			if(MainScreen.hand.getType() == 1){
				putText(null, loc);
			}else if(MainScreen.hand.getType() == 2){
				putDraw(null, loc);
			}else if(MainScreen.hand.getType() == 3){
				putNote(-1, loc);
			}

		}else if(Main.click && MainScreen.hand != null){ // move
			
			if(MainScreen.hand.getType() == 1){
				int x = mouseX + MainScreen.hand.getShift().getX();
				int y = mouseY + MainScreen.hand.getShift().getY();
				
				Location loc = getLocationPercent(x, y, startX, startY, width, height);
				
				drawText(MainScreen.hand.getText(), getLocationPX(loc.getX(), loc.getY(), startX, startY, width, height), g, startX, startY, width, height, mouseX, mouseY);
				
			}else if(MainScreen.hand.getType() == 2){
				
			}else if(MainScreen.hand.getType() == 3){
				
			}
			
		}else if((!Main.click) && MainScreen.hand != null){ // place
			
			Location loc = getLocationPercent(mouseX + MainScreen.hand.getShift().getX(), mouseY + MainScreen.hand.getShift().getY(), startX, startY, width, height);
			
			if(MainScreen.hand.getType() == 1){
				putText(MainScreen.hand.getText(), loc);
			}else if(MainScreen.hand.getType() == 2){
				putDraw(MainScreen.hand.getDraw(), loc);
			}else if(MainScreen.hand.getType() == 3){
				putNote(MainScreen.hand.getNote(), loc);
			}
			MainScreen.hand = null;
			
		}
		
	}
	
	public static Location getLocationPercent(int x, int y, int startX, int startY, int width, int height){
		
		int widthPoints = 100;
		int heightPoints = (int) (((double) height) / ((double) width) * 100);
		
		x -= startX;
		y -= startY;
		
		x = (int) (((double) x) / ((double) width) * widthPoints);
		y = (int) (((double) y) / ((double) height) * heightPoints);
		
		if(x < 0) x = 0;
		if(y < 0) y = 0;
		if(x > widthPoints) x = widthPoints;
		if(y > heightPoints) y = heightPoints;
		
		return new Location(x, y);
		
	}
	public static Location getLocationPX(int x, int y, int startX, int startY, int width, int height){
		
		int widthPoints = 100;
		int heightPoints = (int) (((double) height) / ((double) width) * 100);
		
		x = (int) (((double) x) / ((double) widthPoints) * width);
		y = (int) (((double) y) / ((double) heightPoints) * height);
		
		if(x < 0) x = 0;
		if(y < 0) y = 0;
		if(x > width) x = width;
		if(y > height) y = height;
		
		x += startX;
		y += startY;
		
		return new Location(x, y);
		
	}
	public void putText(AdvancedText text, Location loc){
		
		if(text == null){
			for(Map.Entry<Location, AdvancedText> entry : texts.entrySet()){
				if(entry.getKey().equals(loc)){
					texts.remove(entry.getKey());
					return;
				}
			}
		}else{
			texts.put(loc, text);
		}
		
	}
	public boolean drawText(AdvancedText text, Location loc, Graphics2D g, int startX, int startY, int width, int height, int mouseX, int mouseY){
		
		g.setColor(text.getColor());
		Font font = new Font(text.getFont().getFamily(), 0, (int) (text.getFont().getSize() / 100.0 * MainScreen.zoom));
		
		int[] dimensions = getStringDimensions(g, loc.getX(), loc.getX(), loc.getY(), loc.getY(), text.getContent(), font);
		
		/*if((dimensions[0]/2) + loc.getX() > (width+startX)) loc.setX(startX + width - (dimensions[0]/2) - 5);
		if(loc.getX() - (dimensions[0]/2) < startX) loc.setX(startX + (dimensions[0]/2) + 5);
		if((dimensions[1]/2) + loc.getY() > (height+startY)) loc.setY(startY + height - (dimensions[1]/2) - 5);
		if(loc.getY() - (dimensions[1]/2) < startY) loc.setY(startY + (dimensions[1]/2) + 5);*/
		
		fullCenterString(g, loc.getX(), loc.getX(), loc.getY(), loc.getY(), text.getContent(), font);
		
		if(mouseX > (loc.getX() - dimensions[0]/2) && mouseX < (loc.getX() + dimensions[0]/2)){
			if(mouseY > (loc.getY() - dimensions[1]/2) && mouseY < (loc.getY() + dimensions[1]/2)){
				return true;
			}
		}
		return false;
		
	}
	public void putDraw(Image draw, Location loc){
		
		if(draw == null){
			for(Map.Entry<Location, Image> entry : draws.entrySet()){
				if(entry.getKey().equals(loc)){
					draws.remove(entry.getKey());
					return;
				}
			}
		}else{
			draws.put(loc, draw);
		}
		
	}
	public void putNote(int note, Location loc){
		
		if(note == -1){
			notes.remove(loc);
		}else{
			notes.put(loc, note);
		}
		
	}
	
	private int[] getStringDimensions(Graphics2D g, int minX, int maxX, int minY, int maxY, String s, Font font) {
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());
	   
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
