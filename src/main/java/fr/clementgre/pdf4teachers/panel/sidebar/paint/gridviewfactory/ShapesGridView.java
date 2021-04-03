package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;


import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.CacheHint;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ShapesGridView<T> extends GridView<T>{
    
    public static final String SORT_USE = TR.tr("sorting.sortType.use");
    public static final String SORT_LAST_USE = TR.tr("sorting.sortType.lastUseDate");
    public static final String SORT_FILE_EDIT_TIME = TR.tr("sorting.sortType.fileEditTime");
    public static final String SORT_NAME = TR.tr("sorting.sortType.name");
    public static final String SORT_FOLDER = TR.tr("sorting.sortType.folder");
    public static final String SORT_SIZE= TR.tr("sorting.sortType.fileSize");
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public IntegerProperty cellSize = new SimpleIntegerProperty();
    public final boolean defineCellSizeAsRowNumber;
    protected String filterType = null;
    
    private final SortManager sortManager;
    
    private final List<T> nonFilteredItems = new ArrayList<>();
    
    public ShapesGridView(boolean defineCellSizeAsRowNumber, int cellSize){
        super();
        this.defineCellSizeAsRowNumber = defineCellSizeAsRowNumber;
        sortManager = new SortManager(this::sort, null);
        
        setCellSize(cellSize);
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
    
    public void setItems(List<T> items){
        nonFilteredItems.clear();
        nonFilteredItems.addAll(items);
        updateItemsFiltered();
    }
    public void addItems(List<T> items){
        nonFilteredItems.addAll(items);
        updateItemsFiltered();
    }
    public void removeItems(List<T> items){
        nonFilteredItems.removeAll(items);
        updateItemsFiltered();
    }
    public List<T> getAllItems(){
        return nonFilteredItems;
    }
    
    private void updateCellSize(){
        int columns;
        if(defineCellSizeAsRowNumber) columns = getCellSize();
        else columns = Math.max(1, ((int) getWidth() - 20) / cellSize.get());
        
        double newCellSize = (getWidth() - 20) / columns;
        setCellWidth(newCellSize);
        setCellHeight(newCellSize);
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
}
