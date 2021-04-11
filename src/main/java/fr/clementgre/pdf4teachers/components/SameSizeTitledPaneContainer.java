package fr.clementgre.pdf4teachers.components;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class SameSizeTitledPaneContainer extends VBox{

    public SameSizeTitledPaneContainer(){
        setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
    
        heightProperty().addListener((observable, oldValue, newValue) -> updateTitledPanesSizes());
        
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while(c.next()){
                for(Node n : c.getAddedSubList()){
                    if(n instanceof TitledPane t){
                        t.expandedProperty().addListener((observable, oldValue, newValue) -> updateTitledPanesSizes());
                    }
                }
            }
        });
    }
    
    private void updateTitledPanesSizes(){
        long opened = getListPanes().stream().filter(TitledPane::isExpanded).count();
        double eachOpenedSize = (getHeight() - (4-opened)*28) / opened;
        
        for(TitledPane listPane : getListPanes()){
            if(listPane.isExpanded()){
                listPane.setPrefHeight(eachOpenedSize);
            }else{
                listPane.setPrefHeight(26);
            }
        }
        
    }
    
    private List<TitledPane> getListPanes(){
        return getChildren().stream().filter(o -> o instanceof TitledPane).map(o -> (TitledPane) o).toList();
    }
    
}
