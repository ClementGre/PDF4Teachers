/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridView;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.DefaultFavoriteVectors;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VectorListPane extends ListPane<VectorGridElement>{
    
    private VectorGridView list;
    public VectorListPane(){
        super(MainWindow.paintTab);
    }
    
    
    @Override
    protected void setupGraphics(){
        super.setupGraphics();
    
        list = new VectorGridView(zoomSlider, isFavouriteVectors(), true);
        setupMenu(list);
        
        root.getChildren().add(list);
        list.cellSizeProperty().bindBidirectional(zoomSlider.valueProperty());
        VBox.setVgrow(list, Priority.ALWAYS);
        list.setupSortManager(sortPanel, ShapesGridView.SORT_USE, ShapesGridView.SORT_USE, ShapesGridView.SORT_LAST_USE);
    
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !isLoaded()){
                setLoaded(true);
                list.updateItemsFiltered();
                updateMessage();
            }
        });
    
        if(isFavouriteVectors()){
            setEmptyMessage(TR.tr("paintTab.favouriteVectors.emptyList"));
            setEmptyLink(TR.tr("paintTab.favouriteVectors.loadDefaultVectors"), () -> {
                list.addItems(DefaultFavoriteVectors.getDefaultFavoriteVectors().stream().map(VectorGridElement::new).toList());
            });
        }
        else if(isLastVectors()) setEmptyMessage(TR.tr("paintTab.lastVectors.emptyList"));
        list.getItems().addListener((InvalidationListener) o -> updateMessage());
        updateMessage();
    }
    
    private void updateMessage(){
        if(list.getAllItems().isEmpty()){
            list.setMaxHeight(0);
        }else{
            list.setMaxHeight(Double.MAX_VALUE);
        }
        super.updateMessage(list.getAllItems().isEmpty());
    }
    public void loadVectorsList(ArrayList<VectorData> vectors, boolean updateVisual){
        list.setItems(vectors.stream().map(VectorGridElement::new).toList(), updateVisual);
    }
    
    public static VectorData addFavoriteVector(VectorElement element){
        VectorData linkedVectorData = new VectorData(element.getRealWidth(), element.getRealHeight(), element.getRepeatMode(), element.getResizeMode(),
                element.isDoFill(), element.getFill(), element.getStroke(), element.getStrokeWidth(),
                element.getPath(), element.isInvertX(), element.isInvertY(), element.getArrowLength(), 0, 0);
    
        MainWindow.paintTab.favouriteVectors.getList().addItems(Collections.singletonList(new VectorGridElement(linkedVectorData)));
        return linkedVectorData;
    }
    public static VectorData addLastVector(VectorElement element){
        VectorData linkedVectorData = new VectorData(element.getRealWidth(), element.getRealHeight(), element.getRepeatMode(), element.getResizeMode(),
                element.isDoFill(), element.getFill(), element.getStroke(), element.getStrokeWidth(),
                element.getPath(), element.isInvertX(), element.isInvertY(), element.getArrowLength(), 0, 0);
        
        MainWindow.paintTab.lastVectors.getList().addItems(Collections.singletonList(new VectorGridElement(linkedVectorData)));
        return linkedVectorData;
    }
    
    public boolean isFavoriteVector(VectorElement element){
        for(VectorGridElement gridElement : MainWindow.paintTab.favouriteVectors.getList().getAllItems()){
            if(!gridElement.isFake()) if(gridElement.equals(element)) return true;
        }
        return false;
    }
    @Override
    public void updateGraphics(){
        list.getSortManager().updateGraphics();
    }
    
    @Override
    public VectorGridView getList(){
        return list;
    }
    
    public static NodeMenuItem getPagesMenuItem(){
        VectorGridView list = new VectorGridView(new Slider(2, 6, 4), true, false);
        
        List<VectorGridElement> vectors = MainWindow.paintTab.favouriteVectors.getList().getAllItems();
        if(vectors.size() == 0) return null;
        vectors.sort(VectorGridElement::compareUseWith);
        vectors = vectors.subList(0, Math.min(7, vectors.size()));
        vectors = vectors.stream().map(VectorGridElement::clone).toList();
        
        list.addItems(vectors);
        list.addItems(Collections.singletonList(new VectorGridElement(true)));
        HBox root = new HBox(list);
        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        // 12 is the default cell horizontal padding
        // 14 is the right slider width
        double rowHeight = (210 - 14)/4d;
        PaneUtils.setPosition(list, 0, 0, 210, vectors.size() <= 4 ? rowHeight : 2*rowHeight, false);
        HBox.setMargin(list, new Insets(0, 0, 0, 12));
        NodeMenuItem item = new NodeMenuItem(root, null, false);
        item.removePadding();
        
        list.setOnMouseClicked((e) -> {
            if(item.getParentPopup() != null){
                item.getParentPopup().hide();
            }
        });
        
        return item;
    }
    
}
