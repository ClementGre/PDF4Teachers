package fr.themsou.panel;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import javax.swing.*;

public class LeftbarFiles extends StackPane {
	
	private ArrayList<File> files = new ArrayList<>();
	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;
	private int currentTime = 0;
	private int current = -1;

	private Canvas canvas;
	private GraphicsContext g;

	public LeftbarFiles(){
		canvas = new Canvas(300, 250);
		g = canvas.getGraphicsContext2D();
		getChildren().add(canvas);
		drawShapes();
	}

	public void drawShapes(){
		System.out.println("drawing");
		//Main.footerBar.repaint();

		boolean hasCurrent = false;
		
		setBorder(null);
		int mouseX = 0;
		int mouseY = 0;
		
		g.setFill(Color.rgb(189, 195, 199));
		g.setFill(Color.RED);
		g.fillRect(0, 0, (int) getWidth(), (int) getHeight());
		g.fillOval(0, 0, 100, 100);

		int i;
		for(i = 0; i < files.size(); i++){
			
			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < 0/*Main.leftBarFilesScroll.getWidth()*/){
				
				if(current != i){
					current = i;
					currentTime = 0;
				}
				if(currentTime < 10) currentTime++;
				hasCurrent = true;
				
				g.setFill(Color.rgb(127, 140, 141, currentTime*25));
				g.fillRect(0, i*30, (int) getWidth(), 30);
				
				g.setFill(Color.rgb(44, 62, 80));
				g.setTextAlign(TextAlignment.CENTER);
				g.setFont(new Font("FreeSans", 15));
				g.fillText(files.get(i).getName(), 8, i*30 + 15);
				
				g.drawImage(new Image(Main.devices.getClass().getResource("/img/FilesBar/supprimer.png") + ""), 5 - 40+(currentTime * 4), i*30+5, 20, 20);
				g.drawImage(new Image(Main.devices.getClass().getResource("/img/FilesBar/fermer.png") + ""), 28 - 40+(currentTime * 4), i*30+7, 16, 16);
				
			}else{
				g.setFill(Color.rgb(44, 62, 80));
				g.setTextAlign(TextAlignment.CENTER);
				g.setFont(new Font("FreeSans", 15));
				g.fillText(files.get(i).getName(), 8, i*30 + 15);
			}
			
			
		}
		
		if(!hasCurrent) current = -1;
		
		/*if((maxWidth != width && (currentTime == 10 || !hasCurrent)) || i*30 != height){
			width = maxWidth;
			height = i*30;
			setPreferredSize(new Dimension(width + 10, height));
			Main.leftBarFilesScroll.updateUI();
		}*/
		
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
		//refresh
	}
	public void openFiles(File[] files){
		
		for(File file : files){
			openFile(file);
		}
	}
	public void clearFiles(boolean confirm){
		if(Main.mainScreen.status == -1){
			if(files.contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}
		files = new ArrayList<>();
	}
	public void removeFile(int file, boolean confirm){
		if(Main.mainScreen.status == -1){
			if(files.contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
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
		
		int mouseX = 0;
		int mouseY = 0;

		for(int i = 0; i < files.size(); i++){
			
			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < 0){
				
				if(mouseX > 7 && mouseX < 23){ // Clear Edit
					Edition.clearEdit(files.get(i));
				}else if(mouseX > 28 && mouseX < 44){ // Remove
					removeFile(i, true);
					
				}else if(mouseY > i*30 && mouseY < i*30+30){
					Main.mainScreen.openFile(files.get(i));
				}
				
			}
			
			
			
		}
	}

	

}
