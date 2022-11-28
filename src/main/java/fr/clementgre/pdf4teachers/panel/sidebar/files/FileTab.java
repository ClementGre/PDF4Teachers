/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.files;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertRenderer;
import fr.clementgre.pdf4teachers.document.render.display.PDFPagesRender;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.TextInputAlert;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileTab extends SideTab {
    
    public SortManager sortManager;
    private final VBox pane = new VBox();
    private final GridPane options = new GridPane();
    
    private final VBox info = new VBox();
    
    public FileListView files = new FileListView();
    public ArrayList<File> originalFiles = new ArrayList<>();
    
    public FileTab(){
        super("files", SVGPathIcons.PDF_FILE, 28, 400/500d);
        setContent(pane);
        setup();
        
        
    }
    
    public void setup(){
        
        files.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        files.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        
        getContent().setOnDragDropped((DragEvent e) -> {
            Dragboard db = e.getDragboard();
            if(db.hasFiles()){
                // We need only one good file to accept all. We will do the sorting after.
                for(File file : db.getFiles()){
                    if(isFilePdf(file) || file.isDirectory()){
                        File[] files = db.getFiles().toArray(new File[0]);
                        openFiles(files);
                        if(files.length == 1 && isFilePdf(file)) MainWindow.mainScreen.openFile(file);
                        e.setDropCompleted(true);
                        e.consume();
                        break;
                    }
                }
                // Test for conversion
                ArrayList<File> toConvertFiles = db.getFiles()
                        .stream()
                        .filter(ConvertRenderer::isGoodFormat)
                        .collect(Collectors.toCollection(ArrayList::new));
                if(toConvertFiles.size() != 0){
                    
                    ConvertDocument converter = new ConvertDocument();
                    converter.convertWindow.root.getSelectionModel().select(1);
                    Platform.runLater(() -> {
                        for(File file : toConvertFiles){
                            converter.convertWindow.convertFiles.srcFiles.appendText(file.getAbsolutePath() + "\n");
                        }
                    });
                    
                    e.setDropCompleted(true);
                    e.consume();
                }
                e.consume();
            }
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
    
    public class DirOpener {
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
        
        private void openFileSubDir(File file, int depth){
            int DEPTH_LIMIT = 2;
            if(!isRecursive() || depth > DEPTH_LIMIT || file.listFiles() == null) return;
            
            for(File childrenFile : Objects.requireNonNull(file.listFiles())){
                if(childrenFile.isDirectory()) openFileSubDir(childrenFile, depth + 1);
                else openFileNonDir(childrenFile);
            }
        }
        
        private boolean isRecursive(){
            if(alreadyAsked) return recursive;
            
            CustomAlert alert = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"),
                    TR.tr("dialog.confirmation.openRecursively.header"), TR.tr("dialog.confirmation.openRecursively.details"));
            
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addButton(TR.tr("actions.openAll"), ButtonPosition.DEFAULT);
            
            recursive = alert.getShowAndWaitIsDefaultButton();
            alreadyAsked = true;
            return recursive;
        }
        
    }
    
    private void openFile(File file){
        if(!file.isDirectory()){
            openFileNonDir(file);
        }else{
            new DirOpener(file);
        }
        sortManager.simulateCall();
    }
    
    public void openFileNonDir(File file){
        if(isFilePdf(file) && !files.getItems().contains(file)){
            files.getItems().add(file);
            originalFiles.add(file);
        }
    }
    
    public void openFiles(File[] files){
        for(File file : files){
            openFile(file);
        }
        if(files.length != 0) SideBar.selectTab(this);
    }
    
    public void openFiles(List<File> files){
        for(File file : files){
            openFile(file);
        }
        if(files.size() != 0) SideBar.selectTab(this);
    }
    
    public void clearFiles(){
        files.getItems().clear();
        updateOpenFilesList();
    }
    
    public void removeFile(File file){
        files.getItems().remove(file);
        originalFiles.remove(file);
    }
    
    public static boolean isFilePdf(File file){
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1)
            ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        
        return ext.equalsIgnoreCase("pdf");
    }
    
    // Can be used if a file is renamed for example
    public void replaceFile(File oldFile, File newFile){
        files.getItems().replaceAll(testFile -> {
            if(testFile.getAbsolutePath().equals(oldFile.getAbsolutePath())) return newFile;
            else return testFile;
        });
        originalFiles.replaceAll(testFile -> {
            if(testFile.getAbsolutePath().equals(oldFile.getAbsolutePath())) return newFile;
            else return testFile;
        });
        sortManager.simulateCall();
    }
    
    private void updateOpenFilesList(){
        originalFiles.clear();
        originalFiles.addAll(MainWindow.filesTab.files.getItems());
    }
    
    @SuppressWarnings("unchecked")
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
    public File getCurrentDirAlways(){
        File dir = getCurrentDir();
        return dir != null ? dir : new File("this tile can't exist (I hope ;)!@#$%^&*()_)");
    }
    public ObservableList<File> getOpenedFiles(){
        return files.getItems();
    }
    
    // NAVIGATION
    public void loadPreviousFile(){
        int selected = files.getSelectionModel().getSelectedIndex();
        if(selected <= 0){
            MainWindow.showNotification(AlertIconType.INFORMATION, TR.tr("filesTab.navigation.beginningOfList"), 15);
            return;
        }
        
        File toOpen = files.getItems().get(selected - 1);
        if(toOpen == null) return;
        MainWindow.mainScreen.openFile(toOpen);
    }
    public void loadNextFile(){
        int selected = files.getSelectionModel().getSelectedIndex();
        if(selected == files.getItems().size() - 1){
            MainWindow.showNotification(AlertIconType.INFORMATION, TR.tr("filesTab.navigation.endOfList"), 15);
            return;
        }
        
        File toOpen = files.getItems().get(selected + 1);
        if(toOpen == null) return;
        MainWindow.mainScreen.openFile(toOpen);
    }
    
    
    public void refresh(){
        files.refresh();
    }
    
    public void requestFileRename(File file){
        copyFileDialog(TR.tr("filesTab.fileMenu.rename.dialog.header"), TR.tr("filesTab.fileMenu.rename.dialog.message"), file, true);
    }
    public void requestFileCopy(File file){
        copyFileDialog(TR.tr("filesTab.fileMenu.copy.dialog.header"), TR.tr("filesTab.fileMenu.copy.dialog.message"), file, false);
    }
    
    private void copyFileDialog(String header, String fieldText, File file, boolean move){
        TextInputAlert alert = new TextInputAlert(header, header, fieldText);
        String extension = FilesUtils.getExtension(file);
        String name = FilesUtils.getNameWithoutExtension(file);
        alert.setText(name);
        if(alert.getShowAndWaitIsDefaultButton() && !alert.getText().isBlank() && !(alert.getText().equals(name) && move)){
            File newFile = new File(file.getParentFile().getAbsolutePath() + File.separator + alert.getText() + "." + extension);
        
            // Already exist dialog
            AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(false);
            AlreadyExistDialogManager.ResultType resultType = newFile.exists() ? alreadyExistDialogManager.showAndWait(newFile) : AlreadyExistDialogManager.ResultType.ERASE;
            if(resultType == AlreadyExistDialogManager.ResultType.RENAME) newFile = AlreadyExistDialogManager.rename(newFile);
            if(resultType == AlreadyExistDialogManager.ResultType.RENAME || resultType == AlreadyExistDialogManager.ResultType.ERASE){
                try{
                    // Close file if it is open
                    boolean documentOpen = MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath());
                    if(documentOpen){
                        PDFPagesRender renderer = MainWindow.mainScreen.document.pdfPagesRender;
                        if(!MainWindow.mainScreen.closeFile(true, false)) return; // Close file cancelled.
                        while(!renderer.isClosed()){
                            PlatformUtils.sleepThread(100);
                        }
                    }
                    // Copy/move files
                    if(move){
                        Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        if(Edition.getEditFile(file).exists()) Files.move(Edition.getEditFile(file).toPath(), Edition.getEditFile(newFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }else{ // copy
                        Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        if(Edition.getEditFile(file).exists()) Files.copy(Edition.getEditFile(file).toPath(), Edition.getEditFile(newFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    // AddRename file in files list
                    if(move){
                        MainWindow.filesTab.replaceFile(file, newFile);
                    }else{
                        MainWindow.filesTab.openFile(newFile);
                    }
                
                    // Reopen file
                    if(documentOpen){
                        MainWindow.mainScreen.openFile(newFile);
                    }
                }catch(IOException ex){
                    Log.eAlerted(ex);
                }
            }
        
        }
    }
    
}