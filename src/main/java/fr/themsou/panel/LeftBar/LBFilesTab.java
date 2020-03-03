package fr.themsou.panel.LeftBar;

import java.io.File;
import java.util.Objects;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.SortEvent;
import fr.themsou.utils.SortManager;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LBFilesTab extends Tab {

	private VBox separator = new VBox();
	private HBox options = new HBox();
	public ListView<File> files = new ListView<>();
	boolean titleSortAZ = true;
	boolean orderSortAZ = true;

	public LBFilesTab(){

		setClosable(false);
		setContent(separator);

		setGraphic(Builders.buildImage(getClass().getResource("/img/PDF-Document.png")+"", 0, 25));
		Main.leftBar.getTabs().add(0, this);

		setup();
	}

	public void setup(){
		files.setStyle("-fx-border-width: 0px;");
		files.setPrefWidth(270);
		new LBFilesListView(files);

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

		new SortManager(new SortEvent() {
			@Override public void call(String sortType, boolean order) {
				System.out.println("sort by " + sortType + " - " + (order ? "AZ" : "ZA"));
			}
		}, null, null).setup(options, "", "Ajout", "Nom", "Dossier");


		// import last session files
		if(Main.settings.getOpenedFiles() != null){
			files.getItems().addAll(Main.settings.getOpenedFiles());
		}
		separator.getChildren().addAll(options, files);

		/*files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
			@Override public void changed(ObservableValue<? extends File> observableValue, File lastFile, File newFile) {
				Main.mainScreen.openFile(newFile);
			}
		});*/

	}

	private void openFile(File file){
		
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

		updateOpenFilesList();

	}
	public void clearFiles(boolean confirm){
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
		files.getItems().clear();
		updateOpenFilesList();
	}
	public void removeFile(int file, boolean confirm){
		if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}
		files.getItems().remove(file);
		updateOpenFilesList();

	}
	public void removeFile(File file, boolean confirm){
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
		files.getItems().remove(file);
		updateOpenFilesList();
	}
	
	private boolean isFilePdf(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) 
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);

		return ext.equals("pdf");
	}

	private void updateOpenFilesList(){

		File[] openedFiles = new File[Main.lbFilesTab.files.getItems().size()];
		openedFiles = Main.lbFilesTab.files.getItems().toArray(openedFiles);

		Main.settings.setOpenedFiles(openedFiles);

	}

	

}