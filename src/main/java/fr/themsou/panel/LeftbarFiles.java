package fr.themsou.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.StringDrawing;

public class LeftbarFiles extends JPanel{
	
	private ArrayList<File> files = new ArrayList<>();
	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;
	private int currentTime = 0;
	private int current = -1;
	
	public void paintComponent(Graphics go){

		Main.footerBar.repaint();

		boolean hasCurrent = false;
		
		setBorder(null);
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.setColor(new Color(189, 195, 199));
		g.fillRect(0, 0, getWidth(), getHeight());

		int i;
		for(i = 0; i < files.size(); i++){
			
			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < Main.leftBarFilesScroll.getWidth()){
				
				if(current != i){
					current = i;
					currentTime = 0;
				}
				if(currentTime < 10) currentTime++;
				hasCurrent = true;
				
				g.setColor(new Color(127, 140, 141, currentTime*25));
				g.fillRect(0, i*30, getWidth(), 30);
				
				g.setColor(new Color(44, 62, 80));
				maxWidth = StringDrawing.centerString(g, 8 + (currentTime * 4), i*30, i*30+30, files.get(i).getName(), new Font("FreeSans", Font.PLAIN, 15))[0] + (currentTime * 4) + 8;
				
				g.drawImage(new ImageIcon(Main.devices.getClass().getResource("/img/FilesBar/supprimer.png")).getImage(), 5 - 40+(currentTime * 4), i*30+5, 20, 20, null);
				g.drawImage(new ImageIcon(Main.devices.getClass().getResource("/img/FilesBar/fermer.png")).getImage(), 28 - 40+(currentTime * 4), i*30+7, 16, 16, null);
				
			}else{
				g.setColor(new Color(44, 62, 80));
				maxWidth = StringDrawing.centerString(g, 8, i*30, i*30+30, files.get(i).getName(), new Font("FreeSans", Font.PLAIN, 15))[0] + 8;
			}
			
			
		}
		
		if(!hasCurrent) current = -1;
		
		if((maxWidth != width && (currentTime == 10 || !hasCurrent)) || i*30 != height){
			width = maxWidth;
			height = i*30;
			setPreferredSize(new Dimension(width + 10, height));
			Main.leftBarFilesScroll.updateUI();
		}
		
	}

	public void openFile(File file){
		
		if(!file.isDirectory()){
			if(isFilePdf(file) && !files.contains(file)){
				files.add(file);
			}
		}else{
			
			for(File VFile : Objects.requireNonNull(file.listFiles())){
				
				if(isFilePdf(VFile) && !files.contains(VFile)){
					files.add(VFile);
				}
			}
		}
		repaint();
	}
	public void openFiles(File[] files){
		
		for(File file : files){
			openFile(file);
		}
	}
	public void clearFiles(){
		if(Main.mainScreen.status == -1){
			if(files.contains(Main.mainScreen.document.getFile())){
				Main.mainScreen.closeFile();
			}
		}
		files = new ArrayList<>();
	}
	public void removeFile(int file){
		if(Main.mainScreen.status == -1){
			if(files.contains(Main.mainScreen.document.getFile())){
				Main.mainScreen.closeFile();
			}
		}
		files.remove(file);
	}
	
	private boolean isFilePdf(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) 
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);

		return ext.equals("pdf");
	}

	
	public void mouseReleased(){
		
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;

		for(int i = 0; i < files.size(); i++){
			
			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < Main.leftBarFilesScroll.getWidth()){
				
				if(mouseX > 7 && mouseX < 23){ // Clear Edit
					Edition.getEditFile(files.get(i)).delete();
					//files.get(i).delete();
					removeFile(i);
				}else if(mouseX > 28 && mouseX < 44){ // Remove
					removeFile(i);
					
				}else if(mouseY > i*30 && mouseY < i*30+30){
					Main.mainScreen.openFile(files.get(i));
				}
				
			}
			
			
			
		}
	}

	

}
