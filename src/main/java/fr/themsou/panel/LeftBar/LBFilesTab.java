package fr.themsou.panel.LeftBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.CustomListView;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import javax.swing.*;

public class LBFilesTab extends Tab {

	public ListView<File> files = new ListView<>();
	private int currentTime = 0;
	private int current = -1;


	public LBFilesTab(){

		setClosable(false);
		setContent(files);

		setGraphic(Builders.buildImage(getClass().getResource("/img/PDF-Document.png")+"", 0, 25));
		Main.leftBar.getTabs().add(0, this);

		openFile(new File(System.getProperty("user.home") + "/"));

		setup();
	}

	public void setup(){
		files.setStyle("-fx-border-width: 0px;");
		files.setPrefWidth(270);
		new CustomListView(files);

		files.setOnDragOver(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				if(db.hasFiles()){
					for(File file : db.getFiles()){
						if(isFilePdf(file) || file.isDirectory()){
							e.acceptTransferModes(TransferMode.ANY);
							e.consume();
							return;
						}
					}
				}
				e.consume();
			}
		});
		files.setOnDragDropped(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				if(db.hasFiles()){
					for(File file : db.getFiles()){
						if(isFilePdf(file) || file.isDirectory()){
							File[] files = db.getFiles().toArray(new File[db.getFiles().size()]);
							openFiles(files);
							e.setDropCompleted(true);
							e.consume();
							return;
						}
					}
				}

				e.consume();
			}
		});

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
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
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
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
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
