/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;


import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.CacheHint;
import javafx.scene.control.Slider;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.GridPane;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.controlsfx.control.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ShapesGridView<T> extends GridView<T>{
    
    public static String SORT_USE;
    public static String SORT_LAST_USE;
    public static String SORT_FILE_EDIT_TIME;
    public static String SORT_NAME;
    public static String SORT_FOLDER;
    public static String SORT_SIZE;
    
    public static void setupTranslations(){
        SORT_USE = TR.tr("sorting.sortType.use");
        SORT_LAST_USE = TR.tr("sorting.sortType.lastUseDate");
        SORT_FILE_EDIT_TIME = TR.tr("sorting.sortType.fileEditTime");
        SORT_NAME = TR.tr("sorting.sortType.name");
        SORT_FOLDER = TR.tr("sorting.sortType.folder");
        SORT_SIZE = TR.tr("sorting.sortType.fileSize");
    }
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public IntegerProperty cellSize = new SimpleIntegerProperty();
    public final boolean defineCellSizeAsRowNumber;
    protected String filterType;
    
    private final Slider zoomSlider;
    private final SortManager sortManager;
    
    private final List<T> nonFilteredItems = new ArrayList<>();
    public ShapesGridView(boolean defineCellSizeAsRowNumber, Slider zoomSlider){
        this(defineCellSizeAsRowNumber, zoomSlider, false);
    }
    public ShapesGridView(boolean defineCellSizeAsRowNumber, Slider zoomSlider, boolean hideScrollBarSpace){
        super();
        this.defineCellSizeAsRowNumber = defineCellSizeAsRowNumber;
        this.zoomSlider = zoomSlider;
        sortManager = new SortManager(this::sort, null);
        
        if(hideScrollBarSpace) scrollBarWidth = 14;
        
        setCellSize(getZoomSliderValue());
        zoomSlider.valueProperty().bindBidirectional(cellSizeProperty());
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setMinorTickCount(0);
        zoomSlider.setSnapToTicks(true);
    
        getStyleClass().add(JMetroStyleClass.BACKGROUND);
    
        addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
            e.consume();
            if(defineCellSizeAsRowNumber){
                setZoomSliderValue((int) (getZoomSliderValue() - MathUtils.averageNegativeOrPositive(e.getZoomFactor()-1, -1, 1)) );
            }else{
                setZoomSliderValue((int) (getZoomSliderValue() * e.getZoomFactor()));
            }
        });
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if(e.isControlDown()){
                e.consume();
                if(defineCellSizeAsRowNumber){
                    setZoomSliderValue((int) (getZoomSliderValue() - MathUtils.clamp(e.getDeltaY(), -1, 1)) );
                }else{
                    setZoomSliderValue((int) (getZoomSliderValue() + e.getDeltaY()));
                }
            }
        });
        
        setup();
    }
    
    protected abstract void sort(String sortType, boolean order);
    protected abstract List<T> filter(List<T> items);
    public void updateItemsFiltered(){
        if(filterType == null || filterType.isEmpty()) getItems().setAll(nonFilteredItems);
        else getItems().setAll(filter(nonFilteredItems));
        sort();
    }
    
    protected void setup(){
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setHorizontalCellSpacing(0);
        setVerticalCellSpacing(0);
    
        widthProperty().addListener((observable, oldValue, newValue) -> {
            updateCellSize();
        });
        cellSize.addListener((observable, oldValue, newValue) -> {
            updateCellSize();
        });
        updateCellSize();
    }
    
    private int getZoomSliderValue(){
       return (int) zoomSlider.getValue();
    }
    private void setZoomSliderValue(int value){
        zoomSlider.setValue(value);
    }
    
    public void setItems(List<T> items){
        setItems(items, true);
    }
    public void setItems(List<T> items, boolean updateVisual){
        nonFilteredItems.clear();
        nonFilteredItems.addAll(items);
        if(updateVisual) updateItemsFiltered();
    }
    public void addItems(List<T> items){
        nonFilteredItems.addAll(items);
        updateItemsFiltered();
    }
    public void removeItems(List<T> items){
        nonFilteredItems.removeAll(items);
        updateItemsFiltered();
    }
    public void clear(){
        nonFilteredItems.clear();
        updateItemsFiltered();
    }
    
    public List<T> getAllItems(){
        return nonFilteredItems;
    }
    
    private void updateCellSize(){
        int columns;
        if(defineCellSizeAsRowNumber) columns = getCellSize();
        else columns = Math.max(1, ((int) getWidth() - getScrollBarWidth() - 6) / cellSize.get());
    
        double newCellSize = (getWidth() - getScrollBarWidth() - 6) / columns;
        setCellWidth(newCellSize);
        setCellHeight(newCellSize);
    }
    
    int scrollBarWidth = 14;
    private int getScrollBarWidth(){
        return scrollBarWidth;
    }
    
    public void setupSortManager(GridPane parent, String selectedButtonName, String... buttonsName){
        sortManager.setup(parent, selectedButtonName, buttonsName);
    }
    private void sort(){
        sortManager.simulateCall();
    }
    
    
    public int getCellSize(){
        return cellSize.get();
    }
    public IntegerProperty cellSizeProperty(){
        return cellSize;
    }
    public void setCellSize(int cellSize){
        this.cellSize.set(cellSize);
    }
    public ExecutorService getExecutor(){
        return executor;
    }
    public SortManager getSortManager(){
        return sortManager;
    }
    public String getFilterType(){
        return filterType;
    }
    public void setFilterType(String filterType){
        this.filterType = filterType;
        updateItemsFiltered();
    }
    
    public abstract void resetUseData();
}
