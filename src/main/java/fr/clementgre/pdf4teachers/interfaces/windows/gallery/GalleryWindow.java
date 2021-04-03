package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class GalleryWindow extends Stage{
    
    private final VBox root = new VBox();
    
    private final HBox settings = new HBox();
    private final GridPane sortPanel = new GridPane();
    private final ComboBox<String> filter = new ComboBox<>();
    private final Button editSources = new Button();
    private final EditSourcesPopOver editSourcesPopOver = new EditSourcesPopOver(this);
    
    private final ImageGridView list = new ImageGridView(false, 200, 500);
    
    public GalleryWindow(){
        
        Scene scene = new Scene(root, 545, Main.SCREEN_BOUNDS.getHeight() - 100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight() - 100);
    
        initOwner(Main.window);
        initModality(Modality.APPLICATION_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setWidth(1000);
        setHeight(600);
        setTitle(TR.tr("galleryWindow.title"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);
    
        setOnCloseRequest(e -> editSourcesPopOver.hide(Duration.ZERO));
        
        setup();
        show();
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
        
        editSources.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PENCIL_EDIT, "black", 0, 20, 20, ImageUtils.defaultDarkColorAdjust));
        editSources.setOnAction((e) -> editSourcesPopOver.show(editSources));
    
        PaneUtils.setHBoxPosition(sortPanel, 0, 30, 0);
        PaneUtils.setHBoxPosition(filter, 0, 30, 0);
        PaneUtils.setHBoxPosition(editSources, 30, 30, 0);
        
        settings.setSpacing(10);
        settings.setPadding(new Insets(0, 20, 0, 20));
        settings.getChildren().addAll(sortPanel, new HBoxSpacer(), filter, editSources);
    }
    
    private List<ImageGridElement> getImages(){
        return GalleryManager.getImages().stream().map((img) -> new ImageGridElement(img.getImageId())).collect(Collectors.toList());
    }
}
