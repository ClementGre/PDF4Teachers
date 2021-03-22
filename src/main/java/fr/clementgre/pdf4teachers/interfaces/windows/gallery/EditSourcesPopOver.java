package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.io.File;

public class EditSourcesPopOver extends PopOver{

    VBox pathLists = new VBox();
    
    private GalleryWindow window;
    public EditSourcesPopOver(GalleryWindow window){
        this.window = window;
        
        setTitle(TR.tr("galleryWindow.pathListEditor.title"));
        setAutoHide(true);
        setDetachable(false);
        setHeaderAlwaysVisible(true);
        StyleManager.putStyle(getRoot(), Style.DEFAULT);
        
        
        setOnShowing((e) -> updateGraphics());
    
        pathLists.setMinWidth(300);
        pathLists.getStyleClass().add("content-pane");
        setContentNode(pathLists);
    }
    
    public void updateGraphics(){
        pathLists.getChildren().clear();
        for(String path : GalleryManager.getSavePaths()){
            pathLists.getChildren().add(getPathGraphic(path));
        }
        pathLists.getChildren().add(newPathGraphic());
    }
    
    private HBox getPathGraphic(String path){
        HBox root = new HBox();
        Label text = new Label(FilesUtils.getPathReplacingUserHome(path));
        Button delete = new Button();
        
        delete.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "red", 0, 16, 16));
        
        delete.setOnMouseClicked((e) -> {
            GalleryManager.removeSavePath(path);
            pathLists.getChildren().remove(root);
            window.loadImages();
        });
    
        PaneUtils.setHBoxPosition(text, 0, 26, 5);
        PaneUtils.setHBoxPosition(delete, 26, 26, 5);
        
        root.getChildren().addAll(text, new HBoxSpacer(), delete);
        return root;
    }
    private HBox newPathGraphic(){
        Button add = new Button(TR.tr("galleryWindow.pathListEditor.newButton"));
        PaneUtils.setHBoxPosition(add, 0, 26, new Insets(20, 5, 5, 5));
        
        add.setOnMouseClicked((e) -> {
            File dir = DialogBuilder.showDirectoryDialog(false);
            if(dir != null){
                GalleryManager.addSavePath(dir.getAbsolutePath());
                pathLists.getChildren().add(getPathGraphic(dir.getAbsolutePath()));
                window.loadImages();
            }
        });
        
        HBox root = new HBox();
        root.getChildren().addAll(new HBoxSpacer(), add, new HBoxSpacer());
        return root;
    }

}
