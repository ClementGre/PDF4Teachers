package fr.themsou.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JPanel;

import fr.themsou.main.Main;

@SuppressWarnings("serial")
public class LeftbarFiles extends JPanel{
	
	ArrayList<File> files = new ArrayList<>();
	int width = 0;
	int height = 0;
	
	public void paintComponent(Graphics go){
		
		Main.leftBarFilesScroll.setBorder(null);
		setBorder(null);
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.setColor(new Color(200, 221, 242));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int maxWidth = 0;
		int i = 0;
		for(i = 0; i < files.size(); i++){
			
			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < Main.leftBarFilesScroll.getWidth()){
				g.setColor(new Color(255, 210, 170));
				g.fillRect(0, i*30, getWidth(), 30);
			}
			
			g.setColor(Color.BLACK);
			int[] size = centerString(g, 10, i*30, i*30+30, files.get(i).getName(), new Font("FreeSans", 0, 15));
			if(size[0] >= maxWidth)
				maxWidth = size[0];
		}
		
		if(maxWidth != width || i*30 != height){
			width = maxWidth;
			height = i*30;
			setPreferredSize(new Dimension(width + 20, height));
			Main.leftBarFilesScroll.updateUI();
		}
			
		
	}
	
	public void click(int y) {
		
		for(int i = 0; i < files.size(); i++){
			if(y > i*30 && y < i*30+30){
				Main.mainScreen.openFile(files.get(i));
			}
		}
		
	}

	public void openFile(File file){
		System.out.println("open");
		if(!file.isDirectory()){
			if(!files.contains(file)){
				files.add(file);
			}
		}else{
			
			for(File VFile : file.listFiles()){
				
				if(isFilePdf(VFile) && !files.contains(VFile)){
					files.add(VFile);
				}
				
			}
			
			
		}
		repaint();
		
	}
	
	private boolean isFilePdf(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) 
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        
        if(ext.equals("pdf")){
        	return true;
        }
        
        return false;
    }
	
	public int[] centerString(Graphics g, int X, int minY, int maxY, String s, Font font) {
		
		
	    FontRenderContext frc = new FontRenderContext(null, true, true);

	    Rectangle2D r2D = font.getStringBounds(s, frc);
	    int rWidth = (int) Math.round(r2D.getWidth());
	    int rHeight = (int) Math.round(r2D.getHeight());
	    int rY = (int) Math.round(r2D.getY());

	    int b = ((maxY - minY) / 2) - (rHeight / 2) - rY;

	    g.setFont(font);
	    g.drawString(s, X, minY + b);
	    
	    int retur[] = { rWidth, rHeight };
	    
	    return retur;
	}

	

}
