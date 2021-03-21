package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class SideTab extends Tab{
    
    private final String name;
    
    public SideTab(String name, String iconPath, int width, int height, int[] ratio){
        this.name = name;
        
        setClosable(false);
        
        setGraphic(SVGPathIcons.generateImage(iconPath, "gray", 0, width, height, 0, ratio, ImageUtils.defaultGrayColorAdjust));
        
        setupDragAndDrop(iconPath);
        
        Platform.runLater(() -> {
            if(getTabPane() == null){
                MainWindow.leftBar.getTabs().add(this);
            }
        });
        
    }
    
    public void select(){
        SideBar.selectTab(this);
    }
    
    
    public void setupDragAndDrop(String iconPath){
        
        getGraphic().setOnDragDetected(e -> {
            Dragboard dragboard = getGraphic().startDragAndDrop(TransferMode.MOVE);
            dragboard.setDragView(SVGPathIcons.generateNonSvgImage(iconPath, Color.GRAY, ImageUtils.defaultGrayColorAdjust, .08));
            
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.put(Main.INTERNAL_FORMAT, SideBar.TAB_DRAG_KEY);
            dragboard.setContent(clipboardContent);
            
            SideBar.draggingTab = this;
            SideBar.showDragSpaces();
            e.consume();
        });
        
        getGraphic().setOnDragDone(e -> {
            SideBar.hideDragSpaces();
        });
        
    }
    
    public String getName(){
        return name;
    }
    
    public static SideTab getByName(String name){
        if(MainWindow.filesTab.getName().equals(name)){
            return MainWindow.filesTab;
            
        }else if(MainWindow.textTab.getName().equals(name)){
            return MainWindow.textTab;
            
        }else if(MainWindow.gradeTab.getName().equals(name)){
            return MainWindow.gradeTab;
            
        }else if(MainWindow.paintTab.getName().equals(name)){
            return MainWindow.paintTab;
        }
        return null;
    }
    
}
