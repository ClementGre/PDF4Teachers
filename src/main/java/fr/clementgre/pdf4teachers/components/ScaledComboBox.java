package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.Main;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ScaledComboBox<T> extends ComboBox<T>{
    
    // Bind by default
    public ScaledComboBox(){
        setup(true);
    }
    
    public ScaledComboBox(boolean bind){
        setup(bind);
    }
    
    public ScaledComboBox(ObservableList<T> items, boolean bind){
        super(items);
        setup(bind);
    }
    
    private void setup(boolean bind){
    
        if(bind){
            Main.settings.zoom.valueProperty().addListener((o, oldValue, newValue) -> {
                setStyle("-fx-font-size: " + 12 * Main.settings.zoom.getValue());
            });
        }
        
        setCellFactory(new Callback<>(){
            @Override
            public ListCell<T> call(ListView<T> param){
                return new ListCell<>(){
                    @Override
                    protected void updateItem(T item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty && item != null){
                            setText(item.toString());
                            setStyle("-fx-font-size: " + 12 * Main.settings.zoom.getValue());
                        }
                    }
                };
            }
        });
    }
}
