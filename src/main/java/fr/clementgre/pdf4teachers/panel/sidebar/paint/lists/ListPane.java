package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
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
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public abstract class ListPane<T> extends TitledPane{
    
    public static final int TYPE_FAVOURITES_VECTORS = 0;
    public static final int TYPE_FAVOURITES_IMAGES = 1;
    public static final int TYPE_LAST_VECTORS = 2;
    public static final int TYPE_GALLERY = 3;
    
    private final IntegerProperty type = new SimpleIntegerProperty();
    private boolean isLoaded = false;
    
    // TITLE
    protected final Label title = new Label();
    protected final HBox graphics = new HBox();
    protected final Slider zoomSlider = new Slider(1, 5, 3);
    protected final ToggleButton sortToggleBtn = new ToggleButton("");
    
    // CONTENT
    protected final VBox root = new VBox();
    protected final GridPane sortPanel = new GridPane();
    
    protected PaintTab paintTab;
    public ListPane(PaintTab paintTab){
        this.paintTab = paintTab;
        setExpanded(false);
        getStyleClass().add("paint-tab-titled-pane");
        
        setContent(root);
        Platform.runLater(this::setupGraphics);
    }
    
    protected void setupGraphics(){
        
        title.setText(getTitle());
        PaneUtils.setHBoxPosition(sortToggleBtn,26, 26, new Insets(0, 0, 0, 5));
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
        
        zoomSlider.setBlockIncrement(1);
        zoomSlider.setPrefWidth(75);
        
        graphics.minWidthProperty().bind(widthProperty());
        graphics.setPadding(new Insets(0, 24, 0, 0));
        graphics.setAlignment(Pos.CENTER);
        
        graphics.getChildren().addAll(title, new HBoxSpacer(), zoomSlider, sortToggleBtn);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(graphics);
        
    }
    
    public abstract void updateGraphics();
    
    public abstract ShapesGridView<T> getList();
    
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
    public boolean isLoaded(){
        return isLoaded;
    }
    protected void setLoaded(Boolean loaded){
        this.isLoaded = loaded;
    }
}
