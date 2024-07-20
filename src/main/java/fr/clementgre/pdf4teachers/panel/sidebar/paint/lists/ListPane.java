/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public abstract class ListPane<T> extends TitledPane{
    
    private final IntegerProperty type = new SimpleIntegerProperty();
    private boolean isLoaded;
    
    // TITLE
    protected final Label title = new Label();
    protected final HBox graphics = new HBox();
    protected final Slider zoomSlider = new Slider(1, 6, 4);
    protected final ToggleButton sortToggleBtn = new ToggleButton("");
    
    // CONTENT
    protected final VBox root = new VBox();
    protected final GridPane sortPanel = new GridPane();
    private final Label emptyListLabel = new Label();
    private final Hyperlink emptyListLink = new Hyperlink();
    
    private final VBox messageContainer = new VBox(emptyListLabel, emptyListLink);
    
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
        sortToggleBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SORT, "black", 0, 18, ImageUtils.defaultDarkColorAdjust));
        sortToggleBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("sorting.name")));
        PaneUtils.setVBoxPosition(sortPanel, 0, 26, 0);
    
        if(!sortToggleBtn.isSelected()) sortToggleBtn.setStyle("-fx-background-color: null;");
        else sortToggleBtn.setStyle("");
        sortToggleBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                sortToggleBtn.setStyle("");
                root.getChildren().addFirst(sortPanel);
                setExpanded(true);
            }else{
                sortToggleBtn.setStyle("-fx-background-color: null;");
                root.getChildren().removeFirst();
            }
        });
        
        zoomSlider.setBlockIncrement(1);
        zoomSlider.setPrefWidth(65);
        
        graphics.minWidthProperty().bind(widthProperty());
        graphics.setPadding(new Insets(0, 26, 0, 0));
        graphics.setAlignment(Pos.CENTER);
        
        Region spacer = new HBoxSpacer();
        HBox.setMargin(spacer, new Insets(0, -20, 0, 0));
        graphics.getChildren().addAll(title, spacer, zoomSlider, sortToggleBtn);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        graphics.setCursor(Cursor.DEFAULT);
        setGraphic(graphics);
    
        root.setAlignment(Pos.CENTER);
        
        // Message setup
        emptyListLabel.setTextAlignment(TextAlignment.CENTER);
        emptyListLabel.setWrapText(true);
        VBox.setMargin(emptyListLabel, new Insets(0, 10, 0, 10));
        
        emptyListLink.setTextAlignment(TextAlignment.CENTER);
        emptyListLink.setWrapText(true);
        VBox.setMargin(emptyListLink, new Insets(5, 10, 0, 10));
        
        messageContainer.setAlignment(Pos.CENTER);
    }
    
    protected void setupMenu(ShapesGridView<T> list){
        ContextMenu menu = new ContextMenu();
        
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("menuBar.file.clearList"), true);
        item1.setToolTip(TR.tr("textTab.listMenu.clear.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("textTab.listMenu.clear.resetUseData"), true);
        item2.setToolTip(TR.tr("textTab.listMenu.clear.resetUseData.tooltip"));
        menu.getItems().addAll(item1, item2);
    
        item1.setOnAction(e -> {
            list.clear();
        });
        item2.setOnAction(e -> {
            list.resetUseData();
        });
        NodeMenuItem.setupMenu(menu);
        setContextMenu(menu);
    }
    
    protected void setEmptyMessage(String text){
        emptyListLabel.setText(text);
    }
    protected void setEmptyLink(String text, CallBack onClick){
        emptyListLink.setText(text);
        emptyListLink.setOnAction((e) -> {
            onClick.call();
        });
    }
    protected void updateMessage(boolean empty){
        if(empty){
            if(isLoaded){
                sortToggleBtn.setSelected(false);
                sortToggleBtn.setDisable(true);
            }
    
            if(!root.getChildren().contains(messageContainer))
                root.getChildren().add(messageContainer);
        }else{
            sortToggleBtn.setDisable(false);
    
            root.getChildren().remove(messageContainer);
        }
    }
    
    public abstract void updateGraphics();
    
    public abstract ShapesGridView<T> getList();
    
    private String getTitle(){
        if(isFavouriteVectors()){
            return TR.tr("paintTab.favouriteVectors");
        }
        if(isFavouriteImages()){
            return TR.tr("paintTab.favouriteImages");
        }
        if(isLastVectors()){
            return TR.tr("paintTab.lastsVectors");
        }
        
        // Gallery
        return TR.tr("paintTab.gallery");
    }
    
    public IntegerProperty typeProperty(){
        return type;
    }
    public void setType(int type){
        this.type.set(type);
    }
    public int getType(){
        return type.get();
    }
    public ListPane<?> getFromType(int type){
        return switch(type){
            case 0 -> paintTab.favouriteVectors;
            case 1 -> paintTab.favouriteImages;
            case 2 -> paintTab.lastVectors;
            case 3 -> paintTab.gallery;
            default -> throw new IllegalArgumentException("type " + type + " is not between 0 and 3");
        };
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
        isLoaded = loaded;
    }
}
