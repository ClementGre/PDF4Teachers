package fr.themsou.panel.LeftBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.CustomListView;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import javax.swing.*;

public class LBFilesTab extends Tab {

	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;
	private int currentTime = 0;
	private int current = -1;

	private Canvas canvas;
	private GraphicsContext g;

	public ListView<File> files = new ListView<>();

	public LBFilesTab(){

		setClosable(false);
		setContent(files);

		setGraphic(Builders.buildImage(getClass().getResource("/img/PDF-Document.png")+"", 0, 25));
		Main.leftBar.getTabs().add(0, this);

		/*canvas = new Canvas(300, 250);
		g = canvas.getGraphicsContext2D();
		getChildren().add(canvas);
		drawShapes();*/

		openFile(new File(System.getProperty("user.home") + "/"));

		setup();
	}

		public void repaint(){



	}

	public void setup(){
		files.setStyle("-fx-border-width: 0px;");
		files.setPrefWidth(270);
		new CustomListView(files);
	}

	public void dd(){
		System.out.println("drawing");
		//Main.footerBar.repaint();

		boolean hasCurrent = false;
		int mouseX = 0;
		int mouseY = 0;

		int i;
		for(i = 0; i < files.getItems().size(); i++){

			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < 0/*Main.lbFilesScroll.getWidth()*/){

				if(current != i){
					current = i;
					currentTime = 0;
				}
				if(currentTime < 10) currentTime++;
				hasCurrent = true;

				g.setFill(Color.rgb(127, 140, 141, currentTime*25));

				g.setFill(Color.rgb(44, 62, 80));
				g.setTextAlign(TextAlignment.CENTER);
				g.setFont(new Font("FreeSans", 15));
				g.fillText(files.getItems().get(i).getName(), 8, i*30 + 15);

				g.drawImage(new Image(Main.devices.getClass().getResource("/img/FilesBar/supprimer.png") + ""), 5 - 40+(currentTime * 4), i*30+5, 20, 20);
				g.drawImage(new Image(Main.devices.getClass().getResource("/img/FilesBar/fermer.png") + ""), 28 - 40+(currentTime * 4), i*30+7, 16, 16);

			}else{
				g.setFill(Color.rgb(44, 62, 80));
				g.setTextAlign(TextAlignment.CENTER);
				g.setFont(new Font("FreeSans", 15));
				g.fillText(files.getItems().get(i).getName(), 8, i*30 + 15);
			}


		}

		if(!hasCurrent) current = -1;

		/*if((maxWidth != width && (currentTime == 10 || !hasCurrent)) || i*30 != height){
			width = maxWidth;
			height = i*30;
			setPreferredSize(new Dimension(width + 10, height));
			Main.lbFilesScroll.updateUI();
		}*/

	}

	public void openFile(File file){
		
		if(!file.isDirectory()){
			if(isFilePdf(file) && !files.getItems().contains(file)){
				files.getItems().add(file);
			}
		}else{
			
			for(File VFile : Objects.requireNonNull(file.listFiles())){
				
				if(isFilePdf(VFile) && !files.getItems().contains(VFile)){
					files.getItems().add(VFile);
				}
			}
		}
	}
	public void openFiles(File[] files){
		
		for(File file : files){
			openFile(file);
		}
	}
	public void clearFiles(boolean confirm){
		if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}
		files.getItems().clear();
	}
	public void removeFile(int file, boolean confirm){
		if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}
		files.getItems().remove(file);
	}
	public void removeFile(File file, boolean confirm){
		if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}
		files.getItems().remove(file);
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

		for(int i = 0; i < files.getItems().size(); i++){

			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < 0){

				if(mouseX > 7 && mouseX < 23){ // Clear Edit

				}else if(mouseX > 28 && mouseX < 44){ // Remove
					removeFile(i, true);

				}else if(mouseY > i*30 && mouseY < i*30+30){
					Main.mainScreen.openFile(files.getItems().get(i));
				}

			}



		}
	}

	

}
