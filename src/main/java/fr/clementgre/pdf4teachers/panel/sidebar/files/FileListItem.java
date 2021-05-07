package fr.clementgre.pdf4teachers.panel.sidebar.files;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.components.dialogs.alerts.ConfirmAlert;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Collections;

public class FileListItem extends ListCell<File>{
    
    VBox pane;
    HBox nameBox;
    Label name;
    Label path;
    
    ImageView check = new ImageView();
    ImageView checkLow = new ImageView();
    
    ContextMenu menu;
    EventHandler<MouseEvent> onClick = e -> {
        if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
            MainWindow.mainScreen.openFile(getItem());
    };
    
    public FileListItem(){
        setupGraphic();
    }
    
    public void setupGraphic(){
        
        pane = new VBox();
        nameBox = new HBox();
        name = new Label();
        path = new Label();
        
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
                MainWindow.filesTab.removeFile(file);
                return;
            }
            
            path.setText(FilesUtils.getPathReplacingUserHome(getItem().getParent()));
            
            name.setText(StringUtils.removeAfterLastRegex(file.getName(), ".pdf"));
            if(file.getName().equals(".pdf")) name.setText(".pdf");
            name.setStyle("-fx-font-size: 13;");
            
            nameBox.getChildren().clear();
            
            try{
                double[] elementsCount = Edition.countElements(Edition.getEditFile(file));
                
                if(elementsCount.length > 0){ // has edit file
                    String grade = (elementsCount[4] == -1 ? "?" : MainWindow.format.format(elementsCount[4])) + "/" + MainWindow.format.format(elementsCount[5]);
                    
                    if(elementsCount[0] > 0){ // Has Elements
                        
                        name.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
                        
                        path.setText(path.getText() + " | " + MainWindow.format.format(elementsCount[0]) + " " + TR.tr("elements.name") + " | " + grade);
                        setTooltip(PaneUtils.genToolTip(MainWindow.format.format(elementsCount[0]) + " " + TR.tr("elements.name") + " | " + grade + "\n" + MainWindow.format.format(elementsCount[1]) + " " + TR.tr("elements.name.texts") + "\n" + MainWindow.format.format(elementsCount[2]) + "/" + MainWindow.format.format(elementsCount[6]) + " " + TR.tr("elements.name.grades") + "\n" + MainWindow.format.format(elementsCount[3]) + " " + TR.tr("elements.name.paints")));
                        
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
                        setTooltip(PaneUtils.genToolTip(TR.tr("document.status.noEdit") + " | " + grade + "\n" + MainWindow.format.format(elementsCount[6]) + " " + TR.tr("elements.name.gradeScales")));
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
            
            setOnMouseClicked(onClick);
            
            ContextMenu menu = new ContextMenu();
            
            NodeMenuItem item1 = new NodeMenuItem(TR.tr("actions.open"));
            item1.setToolTip(TR.tr("filesTab.fileMenu.open.tooltip"));
            NodeMenuItem item2 = new NodeMenuItem(TR.tr("actions.remove"));
            item2.setToolTip(TR.tr("filesTab.fileMenu.remove.tooltip"));
            NodeMenuItem item3 = new NodeMenuItem(TR.tr("menuBar.file.deleteEdit"));
            item3.setToolTip(TR.tr("menuBar.file.deleteEdit.tooltip"));
            NodeMenuItem item4 = new NodeMenuItem(TR.tr("actions.deleteFile"));
            item4.setToolTip(TR.tr("filesTab.fileMenu.deleteFile.tooltip"));
            NodeMenuItem item5 = new NodeMenuItem(TR.tr("menuBar.file.export"));
            item5.setToolTip(TR.tr("menuBar.file.export.tooltip"));
            NodeMenuItem item6 = new NodeMenuItem(TR.tr("menuBar.file.clearList"));
            item6.setToolTip(TR.tr("menuBar.file.clearList.tooltip"));
            
            menu.getItems().addAll(item1, item2, item3, item4, item5, new SeparatorMenuItem(), item6);
            NodeMenuItem.setupMenu(menu);
            
            item1.setOnAction(e -> Platform.runLater(() -> MainWindow.mainScreen.openFile(file)));
            
            item2.setOnAction(e -> MainWindow.filesTab.removeFile(file));
            
            item3.setOnAction(e -> Edition.clearEdit(file, true));
            
            item4.setOnAction(e -> {
                if(new ConfirmAlert(true, TR.tr("dialog.confirmation.deleteDocument.header", file.getName())).execute()){
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.closeFile(false);
                        }
                    }
                    MainWindow.filesTab.removeFile(file);
                    Edition.clearEdit(file, false);
                    file.delete();
                }
                
            });
            item5.setOnAction(e -> {
                if(file.exists()){
                    
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.document.save();
                        }
                    }
                    
                    new ExportWindow(Collections.singletonList(file));
                }
                
            });
            item6.setOnAction(e -> MainWindow.filesTab.clearFiles());
            
            setContextMenu(menu);
        }
    }
    
}
