/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.files;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Collections;

public class FileListCell extends ListCell<File> {
    
    private final VBox pane;
    private final HBox nameBox;
    private final Label name;
    private final Label path;
    
    private final ImageView check = new ImageView();
    private final ImageView checkLow = new ImageView();
    
    private final EventHandler<MouseEvent> onClick = e -> {
        if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
            MainWindow.mainScreen.openFile(getItem());
    };
    
    public FileListCell(){
        pane = new VBox();
        nameBox = new HBox();
        name = new Label();
        path = new Label();
        setupGraphic();
    }
    
    public void setupGraphic(){
        
        HBox.setMargin(checkLow, new Insets(0, 4, 0, 0));
        HBox.setMargin(check, new Insets(0, 4, 0, 0));
        
        path.setStyle("-fx-font-size: 9;");
        pane.getChildren().addAll(nameBox, path);
        setStyle("-fx-padding: 2 15;");
    }
    
    @Override
    public void updateItem(File file, boolean empty){
        super.updateItem(file, empty);
        
        if(empty){
            setGraphic(null);
            setTooltip(null);
            setContextMenu(null);
            setOnMouseClicked(null);
            
        }else{
            
            if(!file.exists()){
                // Can't remove item in an update item event -> runLater
                Platform.runLater(() -> {
                    MainWindow.filesTab.removeFile(file);
                });
                return;
            }
            
            path.setText(FilesUtils.getPathReplacingUserHome(getItem().getParent()));
            
            name.setText(StringUtils.removeAfterLastOccurrence(file.getName(), ".pdf"));
            if(file.getName().equals(".pdf")) name.setText(".pdf");
            name.setStyle("-fx-font-size: 13;");
            
            nameBox.getChildren().clear();
            
            try{
                double[] elementsCount = Edition.countElements(Edition.getEditFile(file));
                
                if(elementsCount.length > 0){ // has edit file
                    String grade = (elementsCount[4] == -1 ? "?" : MainWindow.gradesDigFormat.format(elementsCount[4])) + "/" + MainWindow.gradesDigFormat.format(elementsCount[5]);
                    
                    if(elementsCount[0] > 0){ // Has Elements
                        
                        name.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
                        
                        path.setText(path.getText() + " | " + MainWindow.twoDigFormat.format(elementsCount[0]) + " " + TR.tr("elements.name") + " | " + grade);
                        setTooltip(PaneUtils.genToolTip(MainWindow.twoDigFormat.format(elementsCount[0]) + " " + TR.tr("elements.name") + " | " + grade + "\n" + MainWindow.twoDigFormat.format(elementsCount[1]) + " " + TR.tr("elements.name.texts") + "\n" + MainWindow.twoDigFormat.format(elementsCount[2]) + "/" + MainWindow.twoDigFormat.format(elementsCount[6]) + " " + TR.tr("elements.name.grades") + "\n" + MainWindow.twoDigFormat.format(elementsCount[3]) + " " + TR.tr("elements.name.paints")));
                        
                        if(elementsCount[2] == elementsCount[6]){ // Edition completed : Green check
                            if(check.getImage() == null)
                                check.setImage(new Image(getClass().getResource("/img/FilesTab/check.png") + ""));
                            nameBox.getChildren().add(check);
                        }else if(elementsCount[2] >= 1){ // Edition semi-completed : Orange check
                            if(checkLow.getImage() == null)
                                checkLow.setImage(new Image(getClass().getResource("/img/FilesTab/check_low.png") + ""));
                            nameBox.getChildren().add(checkLow);
                        }
                        
                    }else{ // Don't have elements
                        path.setText(path.getText() + " | " + TR.tr("document.status.noEdit") + " | " + grade);
                        setTooltip(PaneUtils.genToolTip(TR.tr("document.status.noEdit") + " | " + grade + "\n" + MainWindow.twoDigFormat.format(elementsCount[6]) + " " + TR.tr("elements.name.gradeScales")));
                    }
                }else{ // don't have edit file
                    path.setText(path.getText() + " | " + TR.tr("document.status.noEdit"));
                    setTooltip(PaneUtils.genToolTip(TR.tr("document.status.noEdit")));
                }
            }catch(Exception e){
                path.setText(path.getText() + " | " + TR.tr("document.status.unableToCheckStatus"));
                setTooltip(PaneUtils.genWrappedToolTip(e.getMessage()));
            }
            nameBox.getChildren().add(name);
            setGraphic(pane);
            
            setOnMouseClicked(new WeakEventHandler<>(onClick));
            
            ContextMenu menu = new ContextMenu();
            
            NodeMenuItem open = new NodeMenuItem(TR.tr("actions.open"), false);
            open.setToolTip(TR.tr("filesTab.fileMenu.open.tooltip"));
            NodeMenuItem rename = new NodeMenuItem(TR.tr("actions.rename"), false);
            rename.setToolTip(TR.tr("filesTab.fileMenu.rename.tooltip"));
            NodeMenuItem copy = new NodeMenuItem(TR.tr("filesTab.fileMenu.copy"), false);
            copy.setToolTip(TR.tr("filesTab.fileMenu.copy.tooltip"));
            NodeMenuItem remove = new NodeMenuItem(TR.tr("actions.remove"), false);
            remove.setToolTip(TR.tr("filesTab.fileMenu.remove.tooltip"));
            NodeMenuItem deleteEdit = new NodeMenuItem(TR.tr("menuBar.file.deleteEdit"), false);
            deleteEdit.setToolTip(TR.tr("menuBar.file.deleteEdit.tooltip"));
            NodeMenuItem deleteFile = new NodeMenuItem(TR.tr("actions.deleteFile"), false);
            deleteFile.setToolTip(TR.tr("filesTab.fileMenu.deleteFile.tooltip"));
            NodeMenuItem export = new NodeMenuItem(TR.tr("menuBar.file.export"), false);
            export.setToolTip(TR.tr("menuBar.file.export.tooltip"));
            NodeMenuItem clearList = new NodeMenuItem(TR.tr("menuBar.file.clearList"), false);
            clearList.setToolTip(TR.tr("menuBar.file.clearList.tooltip"));
            
            menu.getItems().addAll(open, rename, copy, remove, deleteEdit, deleteFile, export, new SeparatorMenuItem(), clearList);
            NodeMenuItem.setupMenu(menu);
            
            open.setOnAction(e -> Platform.runLater(() -> MainWindow.mainScreen.openFile(file)));
            
            rename.setOnAction(e -> MainWindow.filesTab.requestFileRename(file));
            
            copy.setOnAction(e -> MainWindow.filesTab.requestFileCopy(file));
            
            remove.setOnAction(e -> MainWindow.filesTab.removeFile(file));
            
            deleteEdit.setOnAction(e -> Edition.clearEdit(file, true));
            
            deleteFile.setOnAction(e -> {
                if(new ConfirmAlert(true, TR.tr("dialog.confirmation.deleteDocument.header", file.getName())).execute()){
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.closeFile(false, false);
                        }
                    }
                    MainWindow.filesTab.removeFile(file);
                    Edition.clearEdit(file, false);
                    file.delete();
                }
                
            });
            export.setOnAction(e -> {
                if(file.exists()){
                    
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.document.save(true);
                        }
                    }
                    
                    new ExportWindow(Collections.singletonList(file));
                }
                
            });
            clearList.setOnAction(e -> MainWindow.filesTab.clearFiles());
            
            setContextMenu(menu);
        }
    }
    
}
