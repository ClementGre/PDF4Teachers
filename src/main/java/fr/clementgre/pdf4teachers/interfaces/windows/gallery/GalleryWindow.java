package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryWindow extends Stage{
    
    private final VBox root = new VBox();
    
    private final HBox settings = new HBox();
    private final GridPane sortPanel = new GridPane();
    protected final ComboBox<String> filter = new ComboBox<>();
    
    private final ImageGridView list = new ImageGridView(false, 150, 500);
    
    public GalleryWindow(){
        
        Scene scene = new Scene(root, 545, Main.SCREEN_BOUNDS.getHeight() - 100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight() - 100);
    
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setWidth(1000);
        setHeight(600);
        setMinWidth(700);
        setMinHeight(400);
        Main.window.centerWindowIntoMe(this);
        setTitle(TR.tr("galleryWindow.title"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);
        
        filter.setCellFactory(param -> new DirFilterListCell(this));
        filter.setVisibleRowCount(10);
        updateComboBoxItems();
        filter.getSelectionModel().select(0);
        
        filter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) return;
            if(newValue.equals(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton")) && !newValue.equals(oldValue)){
                filter.getSelectionModel().select(oldValue);
            }
        });
        
        list.prefHeightProperty().bind(heightProperty());
        list.prefWidthProperty().bind(widthProperty());
        
        setup();
        show();
    }
    
    public void updateStyle(){
        StyleManager.putStyle(root, Style.DEFAULT);
        list.getSortManager().updateGraphics();
    }
    
    void updateComboBoxItems(){
        List<String> items = GalleryManager.getSavePaths();
        items.sort(String::compareTo);
        items.add(0, TR.tr("galleryWindow.filterAndEditCombo.everywhere"));
        items.add(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton"));
        filter.getItems().setAll(items);
    }
    
    private void setup(){
        setupSettings();
        
        root.getChildren().addAll(settings, list);
        list.getItems().setAll(getImages());
        list.sort();
    }
    
    public void reloadImageList(){
        list.editImages(getImages());
    }
    
    private void setupSettings(){
    
        list.setupSortManager(sortPanel, ShapesGridView.SORT_FOLDER, ShapesGridView.SORT_FOLDER, ShapesGridView.SORT_NAME, ShapesGridView.SORT_USE, ShapesGridView.SORT_FILE_EDIT_TIME, ShapesGridView.SORT_SIZE);
        
        PaneUtils.setHBoxPosition(sortPanel, 0, 26, 0);
        PaneUtils.setHBoxPosition(filter, 0, 26, 0);
        
        settings.setSpacing(10);
        settings.setPadding(new Insets(0, 20, 0, 20));
        settings.getChildren().addAll(sortPanel, new HBoxSpacer(), filter);
    }
    
    private List<ImageGridElement> getImages(){
        return GalleryManager.getImages().stream().map((img) -> new ImageGridElement(img.getImageId())).collect(Collectors.toList());
    }
}
