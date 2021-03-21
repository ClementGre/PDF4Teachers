package fr.clementgre.pdf4teachers.panel.sidebar.files;

import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FileTab extends SideTab{
    
    public SortManager sortManager;
    private VBox pane = new VBox();
    private GridPane options = new GridPane();
    
    private VBox info = new VBox();
    
    public FileListView files = new FileListView();
    public ArrayList<File> originalFiles = new ArrayList<>();
    
    public FileTab(){
        super("files", SVGPathIcons.PDF_FILE, 0, 28, new int[]{400, 500});
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
                        SideBar.selectTab(MainWindow.filesTab);
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
            if(sortType.equals(TR.tr("sorting.sortType.name"))){
                List<File> toSort = files.getItems().stream().collect(Collectors.toList());
                files.getItems().clear();
                files.getItems().addAll(Sorter.sortFilesByName(toSort, order));
            }else if(sortType.equals(TR.tr("sorting.sortType.folder"))){
                List<File> toSort = files.getItems().stream().collect(Collectors.toList());
                files.getItems().clear();
                files.getItems().addAll(Sorter.sortFilesByDir(toSort, order));
            }else if(sortType.equals(TR.tr("sorting.sortType.edit"))){
                List<File> toSort = files.getItems().stream().collect(Collectors.toList());
                files.getItems().clear();
                files.getItems().addAll(Sorter.sortFilesByEdit(toSort, order));
            }else if(sortType.equals(TR.tr("sorting.sortType.addDate"))){
                backOpenFilesList(!order);
            }
            
        }, null);
        sortManager.setup(options, TR.tr("sorting.sortType.addDate"), TR.tr("sorting.sortType.addDate"), TR.tr("sorting.sortType.edit"), "\n", TR.tr("sorting.sortType.name"), TR.tr("sorting.sortType.folder"));
        
        // Info pane
        
        Label infoLabel = new Label(TR.tr("filesTab.noFile.title"));
        infoLabel.setStyle("-fx-font-size: 16;");
        VBox.setMargin(infoLabel, new Insets(0, 0, 10, 0));
        
        Hyperlink openFile = new Hyperlink(TR.tr("menuBar.file.openFiles"));
        openFile.setOnAction(e -> MainWindow.menuBar.file1Open.fire());
        VBox.setMargin(openFile, new Insets(-2, 0, -2, 0));
        
        Hyperlink openDir = new Hyperlink(TR.tr("menuBar.file.openDir"));
        openDir.setOnAction(e -> MainWindow.menuBar.file2OpenDir.fire());
        VBox.setMargin(openDir, new Insets(-2, 0, -2, 0));
        
        Hyperlink convert = new Hyperlink(TR.tr("menuBar.tools.convertImages"));
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
    
    public class DirOpener{
        private int DEEP_LIMIT = 2;
        boolean alreadyAsked = false;
        boolean recursive = false;
        
        public DirOpener(File file){
            for(File childrenFile : Objects.requireNonNull(file.listFiles())){
                if(childrenFile.isDirectory()){
                    openFileSubDir(childrenFile, 1);
                }else{
                    openFileNonDir(childrenFile);
                }
            }
        }
        
        private void openFileSubDir(File file, int deep){
            if(deep > DEEP_LIMIT || file.listFiles() == null) return;
            
            for(File childrenFile : Objects.requireNonNull(file.listFiles())){
                if(childrenFile.isDirectory()) openFileSubDir(childrenFile, deep + 1);
                
                if(isFilePdf(childrenFile) && !files.getItems().contains(childrenFile)){
                    if(isRecursive()) openFile(childrenFile, true);
                    else return;
                }
            }
        }
        
        private boolean isRecursive(){
            if(alreadyAsked) return recursive;
            
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"));
            alert.setHeaderText(TR.tr("dialog.confirmation.openRecursively.header"));
            alert.setContentText(TR.tr("dialog.confirmation.openRecursively.details"));
            
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isEmpty()) return false;
            recursive = result.get() == ButtonType.OK;
            
            alreadyAsked = true;
            return recursive;
        }
        
    }
    
    private void openFile(File file){
        openFile(file, null);
    }
    
    private void openFile(File file, Boolean recursive){
        if(!file.isDirectory()){
            openFileNonDir(file);
        }else{
            new DirOpener(file);
        }
        sortManager.simulateCall();
    }
    
    private void openFileNonDir(File file){
        if(isFilePdf(file) && !files.getItems().contains(file)){
            files.getItems().add(file);
            originalFiles.add(file);
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
    
    private boolean isFilePdf(File file){
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
            if(!MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(TR.getDocFile().getAbsolutePath())){
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