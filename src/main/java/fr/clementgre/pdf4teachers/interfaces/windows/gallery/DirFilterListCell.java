package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.File;

public class DirFilterListCell extends ListCell<String>{
    
    private final GalleryWindow gallery;
    public DirFilterListCell(GalleryWindow galleryWindow){
        this.gallery = galleryWindow;
    }
    
    @Override
    protected void updateItem(String item, boolean empty){
    
        super.updateItem(item, empty);
        if(empty || item == null){
            setGraphic(null);
            setText(null);
            setStyle(null);
        }else if(item.equals(TR.tr("galleryWindow.filterAndEditCombo.everywhere")) || item.equals(TR.tr("galleryWindow.filterAndEditCombo.favourites"))){
            Label text = new Label(item);
            text.setStyle("-fx-font-size: 12;");
            PaneUtils.setHBoxPosition(text, 0, 18, 0);
            setGraphic(text);
            setText(null);
            setStyle(null);
        }else if(item.equals(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton"))){
            Button add = new Button(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton"));
            PaneUtils.setHBoxPosition(add, 0, 30, 0);
    
            add.setOnMousePressed((e) -> {
                Platform.runLater(() -> {
                    File dir = DialogBuilder.showDirectoryDialog(false);
                    if(dir != null){
                        GalleryManager.addSavePath(dir.getAbsolutePath());
                        gallery.updateComboBoxItems();
                        gallery.reloadImageList();
                    }
                    Platform.runLater(gallery.filter::show);
                });
            });
    
            HBox root = new HBox();
            root.getChildren().addAll(new HBoxSpacer(), add, new HBoxSpacer());
            setGraphic(root);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        }else{
            HBox root = new HBox();
            Label text = new Label(FilesUtils.getPathReplacingUserHome(item));
            text.setStyle("-fx-font-size: 12;");
            Button delete = new Button();
    
            delete.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "red", 0, 14, 14));
            delete.setStyle("-fx-background-color: transparent;");
            delete.setOnMouseEntered((e) -> delete.setStyle(null));
            delete.setOnMouseExited((e) -> delete.setStyle("-fx-background-color: transparent;"));
    
            delete.setOnMousePressed((e) -> {
                e.consume();
                GalleryManager.removeSavePath(item);
                gallery.filter.getItems().remove(item);
                gallery.reloadImageList();
        
                // Force ComboBox to reopen after 50 to 500 ms (after the click was released)
                for(int i = 50; i <= 500; i += 50){
                    PlatformUtils.runLaterOnUIThread(i, gallery.filter::show);
                }
            });
    
    
            PaneUtils.setHBoxPosition(text, 0, 26, 0);
            PaneUtils.setHBoxPosition(delete, 26, 26, new Insets(0, 5, 0, 5));
    
            root.setStyle("-fx-padding: -4 0;");
            root.getChildren().addAll(text, new HBoxSpacer(), delete);
            setGraphic(root);
            setText(null);
            setStyle(null);
        }
    }
    
}
