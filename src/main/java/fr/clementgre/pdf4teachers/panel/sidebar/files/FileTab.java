package fr.clementgre.pdf4teachers.panel.sidebar.files;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class FileTab extends SideTab {

	public SortManager sortManager;
	private VBox pane = new VBox();
	private GridPane options = new GridPane();

	private VBox info = new VBox();

	public FileListView files = new FileListView();
	public ArrayList<File> originalFiles = new ArrayList<>();

	public FileTab(){
		super(true, SVGPathIcons.PDF_FILE, 0, 28, new int[]{400, 500});
		setContent(pane);
		setup();


	}

	public void setup(){

		files.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		files.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

		MainWindow.root.setOnDragOver((DragEvent e) -> {
			Dragboard db = e.getDragboard();
			if(db.hasFiles()){
				for(File file : db.getFiles()){
					if(isFilePdf(file) || file.isDirectory() || ConvertRenderer.isGoodFormat(file)){
						e.acceptTransferModes(TransferMode.ANY);
						MainWindow.leftBar.getSelectionModel().select(0);
						e.consume();
						return;
					}
				}
			}
			e.consume();
		});
		MainWindow.root.setOnDragDropped((DragEvent e) -> {
			Dragboard db = e.getDragboard();
			if(db.hasFiles()){
				// We need only one good file to accept all. We will do the sorting after.
				for(File file : db.getFiles()){
					if(isFilePdf(file) || file.isDirectory()){
						File[] files = db.getFiles().toArray(new File[db.getFiles().size()]);
						openFiles(files);
						if(files.length == 1) MainWindow.mainScreen.openFile(files[0]);
						e.setDropCompleted(true);
						e.consume();
						break;
					}
				}
				// Test for conversion
				ArrayList<File> toConvertFiles = new ArrayList<>();
				for(File file : db.getFiles()){
					if(ConvertRenderer.isGoodFormat(file)){
						toConvertFiles.add(file);
					}
				}
				if(toConvertFiles.size() != 0){

					ConvertDocument converter = new ConvertDocument();
					for(File file : toConvertFiles){
						converter.convertWindow.convertFiles.srcFiles.appendText(file.getAbsolutePath() + "\n");
					}
					converter.convertWindow.tabPane.getSelectionModel().select(1);

					e.setDropCompleted(true);
					e.consume();
				}
			}
			e.consume();
		});

		sortManager = new SortManager((sortType, order) -> {
			if(sortType.equals(TR.tr("Nom"))){
				List<File> toSort = files.getItems().stream().collect(Collectors.toList());
				files.getItems().clear();
				files.getItems().addAll(Sorter.sortFilesByName(toSort, order));
			}else if(sortType.equals(TR.tr("Dossier"))){
				List<File> toSort = files.getItems().stream().collect(Collectors.toList());
				files.getItems().clear();
				files.getItems().addAll(Sorter.sortFilesByDir(toSort, order));
			}else if(sortType.equals(TR.tr("Édition"))){
				List<File> toSort = files.getItems().stream().collect(Collectors.toList());
				files.getItems().clear();
				files.getItems().addAll(Sorter.sortFilesByEdit(toSort, order));
			}else if(sortType.equals(TR.tr("Date d'Ajout"))){
				backOpenFilesList(!order);
			}

		}, null);
		sortManager.setup(options, TR.tr("Date d'Ajout"), TR.tr("Date d'Ajout"), TR.tr("Édition"), "\n", TR.tr("Nom"), TR.tr("Dossier"));

		// Info pane

		Label infoLabel = new Label(TR.tr("Aucun fichier ouvert"));
		infoLabel.setStyle("-fx-font-size: 16;");
		VBox.setMargin(infoLabel, new Insets(0, 0, 10, 0));

		Hyperlink openFile = new Hyperlink(TR.tr("Ouvrir un ou plusieurs fichiers"));
		openFile.setOnAction(e -> MainWindow.menuBar.file1Open.fire());
		VBox.setMargin(openFile, new Insets(-2, 0, -2, 0));

		Hyperlink openDir = new Hyperlink(TR.tr("Ouvrir un dossier"));
		openDir.setOnAction(e -> MainWindow.menuBar.file2OpenDir.fire());
		VBox.setMargin(openDir, new Insets(-2, 0, -2, 0));

		Hyperlink convert = new Hyperlink(TR.tr("Convertir"));
		convert.setOnAction(e -> new ConvertDocument());
		VBox.setMargin(convert, new Insets(-2, 0, -2, 0));

		VBox.setMargin(info, new Insets(20, 0, 20, 0));
		info.setAlignment(Pos.CENTER);
		info.getChildren().addAll(infoLabel, openFile, openDir, convert);



		info.visibleProperty().bind(Bindings.size(files.getItems()).isEqualTo(0));
		info.visibleProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue){
				pane.getChildren().remove(info);
			}else if(!pane.getChildren().contains(info)){
				pane.getChildren().add(1, info);
			}
		});


		pane.getChildren().addAll(options, info, files);

	}

	private void openFile(File file){
		
		if(!file.isDirectory()){
			if(isFilePdf(file) && !files.getItems().contains(file)){
				files.getItems().add(file);
				originalFiles.add(file);
				sortManager.simulateCall();
			}
		}else{
			for(File VFile : Objects.requireNonNull(file.listFiles())){
				if(isFilePdf(VFile) && !files.getItems().contains(VFile)){
					files.getItems().add(VFile);
					originalFiles.add(VFile);
				}
			}
			sortManager.simulateCall();
		}
	}
	public void openFiles(File[] files){
		for(File file : files){
			openFile(file);
		}
	}
	public void openFiles(List<File> files){
		for(File file : files){
			openFile(file);
		}
	}
	public void clearFiles(){
		files.getItems().clear();
		updateOpenFilesList();
	}
	public void removeFile(File file){
		files.getItems().remove(file);
		originalFiles.remove(file);
	}
	
	private boolean isFilePdf(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1)
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);

		return ext.equalsIgnoreCase("pdf");
	}

	private void updateOpenFilesList(){
		originalFiles.clear();
		originalFiles.addAll(MainWindow.filesTab.files.getItems());
	}
	public void backOpenFilesList(boolean reverse){
		files.getItems().clear();
		ArrayList<File> openedFilesList = (ArrayList<File>) originalFiles.clone();
		if(reverse) Collections.reverse(openedFilesList);
		for(File file : openedFilesList){
			files.getItems().add(file);
		}
	}

	public File getCurrentDir(){
		if(MainWindow.mainScreen.hasDocument(false)){
			if(!MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(LanguageWindow.getDocFile().getAbsolutePath())){
				return MainWindow.mainScreen.document.getFile().getParentFile();
			}
		}
		if(files.getItems().size() != 0) return files.getItems().get(0).getParentFile();
		return null;
	}

	public ObservableList<File> getOpenedFiles(){
		return files.getItems();
	}
	public void refresh(){
		files.refresh();
	}

}