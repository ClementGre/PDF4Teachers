package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.*;
import fr.clementgre.pdf4teachers.utils.svg.DefaultFavoriteVectors;
import javafx.beans.InvalidationListener;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class VectorListPane extends ListPane<VectorGridElement>{
    
    private VectorGridView list;
    public VectorListPane(){
        super(MainWindow.paintTab);
    }
    
    
    @Override
    protected void setupGraphics(){
        super.setupGraphics();
    
        list = new VectorGridView(zoomSlider, isFavouriteVectors());
        setupMenu(list);
        
        root.getChildren().add(list);
        list.cellSizeProperty().bindBidirectional(zoomSlider.valueProperty());
        VBox.setVgrow(list, Priority.ALWAYS);
        list.setupSortManager(sortPanel, ShapesGridView.SORT_USE, ShapesGridView.SORT_USE, ShapesGridView.SORT_LAST_USE);
    
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !isLoaded()){
                setLoaded(true);
                list.updateItemsFiltered();
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
                element.getPath(), element.isInvertX(), element.isInvertY(), 0, 0);
    
        MainWindow.paintTab.favouriteVectors.getList().addItems(Collections.singletonList(new VectorGridElement(linkedVectorData)));
        return linkedVectorData;
    }
    public static VectorData addLastVector(VectorElement element){
        VectorData linkedVectorData = new VectorData(element.getRealWidth(), element.getRealHeight(), element.getRepeatMode(), element.getResizeMode(),
                element.isDoFill(), element.getFill(), element.getStroke(), element.getStrokeWidth(),
                element.getPath(), element.isInvertX(), element.isInvertY(), 0, 0);
        
        MainWindow.paintTab.lastVectors.getList().addItems(Collections.singletonList(new VectorGridElement(linkedVectorData)));
        return linkedVectorData;
    }
    
    public boolean isFavoriteVector(VectorElement element){
        for(VectorGridElement gridElement : MainWindow.paintTab.favouriteVectors.getList().getAllItems()){
            if(gridElement.equals(element)) return true;
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
    
}
