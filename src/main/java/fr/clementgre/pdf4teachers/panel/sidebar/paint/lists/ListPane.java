package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.GridView;

public abstract class ListPane<T> extends TitledPane{
    
    private final IntegerProperty type = new SimpleIntegerProperty();
    
    // TITLE
    protected Label title = new Label();
    protected HBox graphics = new HBox();
    protected SortManager sortManager;
    protected ToggleButton sortToggleBtn = new ToggleButton("");
    
    // CONTENT
    protected final VBox root = new VBox();
    protected final GridView<T> list = new GridView<>();
    protected GridPane sortPanel = new GridPane();
    
    
    public ListPane(){
        getStyleClass().add("paint-tab-titled-pane");
        
        sortManager = new SortManager((sortType, order) -> {
        
        }, null);
        sortManager.setup(sortPanel, ShapesGridView.SORT_USE, ShapesGridView.SORT_USE, ShapesGridView.SORT_FILE_EDIT_TIME);
        
        root.getChildren().add(list);
        setContent(root);
    
        Platform.runLater(this::setupGraphics);
    }
    
    private void setupGraphics(){
        title.setText(getTitle());
        
        PaneUtils.setPosition(sortToggleBtn, 0, 0, 26, 26, true);
        sortToggleBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SORT, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        sortToggleBtn.setTooltip(PaneUtils.genToolTip(TR.tr("sorting.name")));
    
        if(!sortToggleBtn.isSelected()) sortToggleBtn.setStyle("-fx-background-color: null;");
        else sortToggleBtn.setStyle("");
        sortToggleBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                sortToggleBtn.setStyle("");
                root.getChildren().add(0, sortPanel);
                setExpanded(true);
            }else{
                sortToggleBtn.setStyle("-fx-background-color: null;");
                root.getChildren().remove(0);
            }
        });
        
        graphics.minWidthProperty().bind(widthProperty());
        graphics.setPadding(new Insets(0, 30, 0, 0));
        graphics.setAlignment(Pos.CENTER);
        
        graphics.getChildren().addAll(title, new HBoxSpacer(), sortToggleBtn);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(graphics);
        
    }
    
    public void updateGraphics(){
        sortManager.updateGraphics();
    }
    
    public GridView<T> getList(){
        return list;
    }
    
    private String getTitle(){
        if(isFavouriteVectors()){
            return TR.tr("paintTab.favouriteVectors");
        }else if(isFavouriteImages()){
            return TR.tr("paintTab.favouriteImages");
        }else if(isLastVectors()){
            return TR.tr("paintTab.lastsVectors");
        }else{ // Gallery
            return TR.tr("paintTab.gallery");
        }
    }
    
    public IntegerProperty typeProperty(){
        return type;
    }
    public void setType(int type){
        this.type.set(type);
    }
    public int getType(){
        return this.type.get();
    }
    public boolean isFavouriteVectors(){
        return getType() == 0;
    }
    public boolean isFavouriteImages(){
        return getType() == 1;
    }
    public boolean isLastVectors(){
        return getType() == 2;
    }
    public boolean isGallery(){
        return getType() == 3;
    }
    
}
