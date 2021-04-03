package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class NodeRadioMenuItem extends NodeMenuItem{
    
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private ImageView SELECTED_IMAGE = ImageUtils.buildImage(NodeRadioMenuItem.class.getResource("/img/MenuBar/yes.png") + "", 16, 16);
    private ImageView NONSELECTED_IMAGE = ImageUtils.buildImage(NodeRadioMenuItem.class.getResource("/img/MenuBar/no.png") + "", 16, 16);
    
    private boolean autoUpdate;
    
    public NodeRadioMenuItem(HBox node, String text, boolean fat, boolean autoUpdate){
        super(node, text, fat, false);
        this.autoUpdate = autoUpdate;
        setup();
    }
    public NodeRadioMenuItem(HBox node, String text, boolean fat, boolean autoUpdate, boolean hideOnClick){
        super(node, text, fat, hideOnClick);
        this.autoUpdate = autoUpdate;
        setup();
    }
    
    private void setup(){
        
        selected.addListener((ObservableValue<? extends Boolean> observable, Boolean oldSelected, Boolean selected) -> {
            if(selected) setLeftData(SELECTED_IMAGE);
            else setLeftData(NONSELECTED_IMAGE);
        });
        
        if(autoUpdate){
            getNode().setOnMouseClicked((e) -> {
                setSelected(!isSelected());
            });
        }
        
        if(isSelected()) setLeftData(SELECTED_IMAGE);
        else setLeftData(NONSELECTED_IMAGE);
        
    }
    
    public boolean isSelected(){
        return selected.get();
    }
    
    public BooleanProperty selectedProperty(){
        return selected;
    }
    
    public void setSelected(boolean selected){
        this.selected.set(selected);
    }
}
