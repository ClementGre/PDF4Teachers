package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.CacheHint;
import org.controlsfx.control.GridView;

public class ShapesGridView<T> extends GridView<T>{

    public IntegerProperty cellSize = new SimpleIntegerProperty();
    public final boolean defineCellSizeAsRowNumber;
    
    public ShapesGridView(boolean defineCellSizeAsRowNumber){
        super();
        this.defineCellSizeAsRowNumber = defineCellSizeAsRowNumber;
        
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setHorizontalCellSpacing(0);
        setVerticalCellSpacing(0);
    
        //setCellFactory(param -> new ImageGridCell());
        
        widthProperty().addListener((observable, oldValue, newValue) -> {
            updateCellSize();
        });
        updateCellSize();
    }
    
    private void updateCellSize(){
        int columns = ((int) getWidth() - 20) / cellSize.get(); // review
        columns = Math.max(1, columns);
        double newCellSize = (getWidth() - 20) / columns;
        setCellWidth(newCellSize);
        setCellHeight(newCellSize);
    }

}
