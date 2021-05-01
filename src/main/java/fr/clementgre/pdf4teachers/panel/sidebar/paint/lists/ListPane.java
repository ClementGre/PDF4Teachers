package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public abstract class ListPane<T> extends TitledPane{
    
    private final IntegerProperty type = new SimpleIntegerProperty();
    private boolean isLoaded = false;
    
    // TITLE
    protected final Label title = new Label();
    protected final HBox graphics = new HBox();
    protected final Slider zoomSlider = new Slider(1, 6, 4);
    protected final ToggleButton sortToggleBtn = new ToggleButton("");
    
    // CONTENT
    protected final VBox root = new VBox();
    protected final GridPane sortPanel = new GridPane();
    
    protected PaintTab paintTab;
    public ListPane(PaintTab paintTab){
        this.paintTab = paintTab;
        setExpanded(false);
        getStyleClass().add("paint-tab-titled-pane");
        
        setMaxHeight(Double.MAX_VALUE);
        setContent(root);
        setAnimated(false);
        Platform.runLater(this::setupGraphics);
    }
    
    protected void setupGraphics(){
        
        title.setText(getTitle());
        PaneUtils.setHBoxPosition(sortToggleBtn,26, 26, new Insets(0, 0, 0, 5));
        sortToggleBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SORT, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        sortToggleBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("sorting.name")));
        PaneUtils.setVBoxPosition(sortPanel, 0, 26, 0);
    
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
        
        Region spacer = new HBoxSpacer();
        HBox.setMargin(spacer, new Insets(0, -20, 0, 0));
        graphics.getChildren().addAll(title, spacer, zoomSlider, sortToggleBtn);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        graphics.setCursor(Cursor.DEFAULT);
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
    public ListPane<?> getFromType(int type){
        if(type == 0) return paintTab.favouriteVectors;
        if(type == 1) return paintTab.favouriteImages;
        if(type == 2) return paintTab.lastVectors;
        if(type == 3) return paintTab.gallery;
        throw new RuntimeException("type " + type + " is not between 0 and 3");
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
    protected void setLoaded(boolean loaded){
        this.isLoaded = loaded;
    }
}
